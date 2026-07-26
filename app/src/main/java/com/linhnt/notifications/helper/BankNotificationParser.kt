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
        "(?i)(?:VND|VNĐ|Đ|₫|\\$|\\(VND\\)|\\(VNĐ\\))?\\s*([+-])?\\s*(\\d{1,3}(?:[.,\\s]\\d{3})*|\\d+)(?:\\s*(?:VND|VNĐ|Đ|₫|\\$))?",
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    private val creditWords = listOf(
        "nhan", "ghi co", "credit", "tien vao", "bien dong tang", "cong tien", "vua duoc cong", "tang", "ps:+"
    )

    private val debitWords = listOf(
        "ghi no", "debit", "da chuyen", "chuyen di", "thanh toan", "rut tien", "tru tien", "chi tieu", "giam", "ps:-"
    )

    fun parse(packageName: String, rawContent: String, sourceNames: List<String>): ParsedTransaction? {
        val initialAppName = SupportedBankApps.displayName(packageName) ?: return null
        if (sourceNames.isEmpty()) return null

        val content = normalize(rawContent)
        val sourceAccount = findSourceAndAccount(content, sourceNames) ?: return null
        val amount = findAmount(content, sourceAccount.markerStart) ?: return null

        // Nếu là TestApp, ưu tiên lấy tên ngân hàng tìm được trong nội dung làm appName
        val finalAppName = if (packageName == SupportedBankApps.TEST) {
            sourceAccount.source
        } else {
            initialAppName
        }

        return ParsedTransaction(
            app = finalAppName,
            source = sourceAccount.source,
            account = sourceAccount.account,
            amount = amount
        )
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
            .replace('\u00A0', ' ')
            .replace(Regex("(?i)QR\\s*[-:]\\s*"), "")
            .replace(Regex("[\\t\\r\\n]+"), " ")
            .trim()
    }

    private fun removeDiacritics(str: String): String {
        val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("").lowercase(Locale.ROOT)
    }

    private fun findSourceAndAccount(content: String, sourceNames: List<String>): SourceAccount? {
        val safeAlternatives = sourceNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .sortedByDescending { it.length }
            .joinToString("|") { Pattern.quote(it) }

        if (safeAlternatives.isEmpty()) return null

        // 1. Tìm source (ngân hàng)
        val sourcePattern = Pattern.compile("($safeAlternatives)", Pattern.CASE_INSENSITIVE)
        val sourceMatcher = sourcePattern.matcher(content)
        var bestSource: String? = null
        var sourceStart = -1
        if (sourceMatcher.find()) {
            bestSource = sourceMatcher.group(1)
            sourceStart = sourceMatcher.start()
        }
        
        if (bestSource == null) return null

        // 2. Tìm số tài khoản (thường đi sau các từ khóa như TK, STK, Số TK, Account)
        // Hoặc các chuỗi số/ký tự đặc biệt đứng sau tên ngân hàng
        val accountChars = "[\\p{L}\\p{N}_.@'’\\-*]+"
        // Hỗ trợ trường hợp có tên ngân hàng kẹt giữa: TK VCB 123456
        val accountPattern = Pattern.compile(
            "(?:TK|STK|SO TK|ACCOUNT|Tài khoản)[:\\-.\u00a0\\s]+(?:(?:$bestSource)[:\\-.\u00a0\\s]+)?($accountChars)",
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
        val accountMatcher = accountPattern.matcher(content)
        var bestAccount: String? = null
        
        if (accountMatcher.find()) {
            bestAccount = accountMatcher.group(1)
        } else {
            // Nếu không thấy từ khóa TK, thử tìm chuỗi số dài sau source
            val fallbackPattern = Pattern.compile(
                "(?:$bestSource)[:\\-.\u00a0\\s]+($accountChars)",
                Pattern.CASE_INSENSITIVE
            )
            val fallbackMatcher = fallbackPattern.matcher(content)
            if (fallbackMatcher.find()) {
                bestAccount = fallbackMatcher.group(1)
            }
        }

        val account = bestAccount
            ?.trim()
            ?.trimEnd('.', ',', ';', ':', '"', '”', '\'', ')', ']')
            .orEmpty()

        if (account.isEmpty()) return null
        
        return SourceAccount(bestSource, account, sourceStart)
    }

    private fun findAmount(content: String, sourceMarkerStart: Int): String? {
        val normalizedNoDiacritics = removeDiacritics(content)
        val matcher = amountPattern.matcher(content)
        val candidates = ArrayList<AmountCandidate>()

        while (matcher.find()) {
            val sign = matcher.group(1).orEmpty()
            val digits = matcher.group(2).orEmpty().filter(Char::isDigit)
            if (digits.isEmpty() || digits.all { it == '0' }) continue

            val windowStart = (matcher.start() - 60).coerceAtLeast(0)
            val prefix = normalizedNoDiacritics.substring(windowStart, matcher.start())
            val suffix = normalizedNoDiacritics.substring(
                matcher.end(),
                (matcher.end() + 20).coerceAtMost(normalizedNoDiacritics.length)
            )
            
            var score = 0

            // Ưu tiên số tiền có dấu biến động
            if (sign == "+") score += 150
            if (sign == "-") score -= 200 // Thường app chỉ cần bắt tiền vào, nếu muốn cả 2 thì sửa lại

            // Chấm điểm theo từ khóa
            if (creditWords.any(prefix::contains)) score += 100
            if (debitWords.any(prefix::contains)) score -= 150
            
            // Từ khóa nhận diện số dư (cần trừ điểm nặng)
            val isBalance = prefix.contains("so du") || prefix.contains("sd:") || 
                           prefix.contains("sd kha dung") || suffix.contains("so du") || 
                           suffix.contains("sd:") || suffix.contains("sd kha dung")
            
            if (isBalance) {
                // Nếu có dấu + hoặc - rõ ràng, thì chữ "Số dư" ở prefix thường là "Số dư TK ... thay đổi +..."
                // Trường hợp này không trừ điểm nặng nếu là prefix.
                if (sign.isNotEmpty() && prefix.contains("so du")) {
                    score -= 20
                } else {
                    score -= 120
                }
            }
            
            // Từ khóa "Phát sinh" (PS) - Thường đi kèm với số tiền giao dịch
            if (prefix.contains("ps")) {
                score += 80
            }

            // Khoảng cách tới tên ngân hàng (càng gần càng tốt)
            val distance = Math.abs(matcher.start() - sourceMarkerStart)
            score += (50 - distance / 15).coerceAtLeast(0)

            candidates.add(AmountCandidate(digits, score, matcher.start()))
        }


        // Ưu tiên ứng viên có điểm cao nhất và điểm phải dương (để lọc số dư)
        val best = candidates.filter { it.score > 0 }.maxByOrNull { it.score } ?: return null

        return best.amount
    }
}

