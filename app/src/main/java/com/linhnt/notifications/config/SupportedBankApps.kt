package com.linhnt.notifications.config

object SupportedBankApps {
    const val MOMO = "com.mservice.momotransfer"
    const val VCB = "com.VCB"
    const val TPBANK = "com.tpb.mb.gprsandroid"
    const val MBBANK = "com.mbmobile"
    const val VPBANK = "com.vnpay.vpbankonline"
    const val ACB = "mobile.acb.com.vn"

    private val apps = mapOf(
        MOMO to "Momo",
        VCB to "VCB",
        TPBANK to "TPBank",
        MBBANK to "MBBank",
        VPBANK to "VPBank",
        ACB to "ACB"
    )

    fun isSupported(packageName: String): Boolean = apps.containsKey(packageName)

    fun displayName(packageName: String): String? = apps[packageName]

    fun allPackages(): Set<String> = apps.keys
}
