package com.linhnt.notifications.helper

import com.linhnt.notifications.config.SupportedBankApps
import com.linhnt.notifications.model.ParsedTransaction
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

object BankNotificationParser {
    private data class SourceAccount(
        val source: String,
        val account: String,
        val markerStart: Int
    )

    private data class AmountCandidate(
        val amount: String,
        val score: Int,
        val start: Int
    )

    private val amountPattern = Pattern.compile(
        "([+-]?)\\s*((?:\\d{1,3}(?:[.,\\s]\\d{3})+)|\\d+)\\s*(?:VND|VNĐ|Đ|₫)",
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    private val creditWords = listOf(
        "nhận", "ghi có", "credit", "tiền vào", "biến động tăng", "cộng tiền", "vừa được cộng"
    )

    private val debitWords = listOf(
        "ghi nợ", "debit", "đã chuyển", "chuyển đi", "thanh toán", "rút tiền", "trừ tiền", "chi tiêu"
    )

    fun parse(packageName: String, rawContent: String, sourceNames: List<String>): ParsedTransaction? {
        val appName = SupportedBankApps.displayName(packageName) ?: return null
        if (sourceNames.isEmpty()) return null

        val content = normalize(rawContent)
        val sourceAccount = findSourceAndAccount(content, sourceNames) ?: return null
        val amount = findAmount(content, sourceAccount.markerStart) ?: return null

        return ParsedTransaction(
            app = appName,
            source = sourceAccount.source,
            account = sourceAccount.account,
            amount = amount
        )
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replace('\u00A0', ' ')
            .replace(Regex("(?i)QR\\s*[-:]\\s*"), "")
            .replace(Regex("[\\t\\r]+"), " ")
            .trim()
    }

    private fun findSourceAndAccount(content: String, sourceNames: List<String>): SourceAccount? {
        val safeAlternatives = sourceNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .sortedByDescending { it.length }
            .joinToString("|") { Pattern.quote(it) }

        if (safeAlternatives.isEmpty()) return null

        val accountChars = "[\\p{L}\\p{N}_.@'’\\-]+"
        val pattern = Pattern.compile(
            "(?:[\\\"“”']\\s*)?($safeAlternatives)\\s+($accountChars)(?:\\s*[\\\"“”'])?",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE or Pattern.DOTALL
        )

        val matcher = pattern.matcher(content)
        if (!matcher.find()) return null

        val source = matcher.group(1)?.trim().orEmpty()
        val account = matcher.group(2)
            ?.trim()
            ?.trimEnd('.', ',', ';', ':', '"', '”', '\'', ')', ']')
            .orEmpty()

        if (source.isEmpty() || account.isEmpty()) return null
        return SourceAccount(source, account, matcher.start())
    }

    private fun findAmount(content: String, sourceMarkerStart: Int): String? {
        val lower = content.lowercase(Locale.ROOT)
        val matcher = amountPattern.matcher(content)
        val candidates = ArrayList<AmountCandidate>()

        while (matcher.find()) {
            val sign = matcher.group(1).orEmpty()
            val digits = matcher.group(2).orEmpty().filter(Char::isDigit)
            if (digits.isEmpty() || digits.all { it == '0' }) continue

            val windowStart = (matcher.start() - 80).coerceAtLeast(0)
            val prefix = lower.substring(windowStart, matcher.start())
            var score = 0

            if (sign == "+") score += 120
            if (sign == "-") score -= 150
            if (creditWords.any(prefix::contains)) score += 90
            if (debitWords.any(prefix::contains)) score -= 100
            if (prefix.contains("số dư") && !prefix.contains("biến động")) score -= 35

            if (matcher.end() <= sourceMarkerStart) {
                score += 30
                val distance = sourceMarkerStart - matcher.end()
                score += (30 - distance / 40).coerceAtLeast(0)
            } else {
                score -= 20
            }

            candidates.add(AmountCandidate(digits, score, matcher.start()))
        }

        val best = candidates.maxWithOrNull(
            compareBy<AmountCandidate> { it.score }.thenBy { it.start }
        ) ?: return null

        return if (best.score >= 0) best.amount else null
    }
}
