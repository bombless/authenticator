package com.bombless.authenticator.data

import android.net.Uri
import java.util.UUID

data class OtpEntry(
    val id: String = UUID.randomUUID().toString(),
    val issuer: String,
    val account: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
) {
    val title: String get() = issuer.ifBlank { account }
    val subtitle: String get() = if (issuer.isNotBlank() && account.isNotBlank()) account else ""
}

object OtpUriParser {
    fun parse(raw: String): OtpEntry {
        val uri = Uri.parse(raw.trim())
        require(uri.scheme == "otpauth" && uri.host == "totp") { "仅支持 TOTP 类型二维码" }
        val label = Uri.decode(uri.path.orEmpty().removePrefix("/"))
        val issuerInLabel = label.substringBefore(':', "")
        val account = if (issuerInLabel.isNotEmpty()) label.substringAfter(':') else label
        val issuer = uri.getQueryParameter("issuer").orEmpty().ifBlank { issuerInLabel }
        val secret = uri.getQueryParameter("secret").orEmpty().replace(" ", "").uppercase()
        require(secret.isNotBlank() && secret.matches(Regex("[A-Z2-7]+=*"))) { "二维码中没有有效的 Base32 密钥" }
        val algorithm = uri.getQueryParameter("algorithm")?.uppercase() ?: "SHA1"
        require(algorithm in setOf("SHA1", "SHA256", "SHA512")) { "不支持的 OTP 算法" }
        val digits = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
        val period = uri.getQueryParameter("period")?.toIntOrNull() ?: 30
        require(digits in 6..8 && period in 1..300) { "二维码参数无效" }
        return OtpEntry(issuer = issuer, account = account, secret = secret, algorithm = algorithm, digits = digits, period = period)
    }
}
