package com.bombless.authenticator.data

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Totp {
    private const val BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun code(entry: OtpEntry, nowMillis: Long = System.currentTimeMillis()): String {
        val key = decodeBase32(entry.secret)
        val counter = nowMillis / 1000L / entry.period
        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance("Hmac${entry.algorithm}")
        mac.init(SecretKeySpec(key, "Hmac${entry.algorithm}"))
        val hash = mac.doFinal(data)
        val offset = hash.last().toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            (hash[offset + 3].toInt() and 0xff)
        val modulus = (1..entry.digits).fold(1L) { acc, _ -> acc * 10L }
        return (binary.toLong() % modulus).toString().padStart(entry.digits, '0')
    }

    fun remainingSeconds(entry: OtpEntry, nowMillis: Long = System.currentTimeMillis()): Int {
        val elapsed = (nowMillis / 1000L) % entry.period
        return (entry.period - elapsed).toInt()
    }

    private fun decodeBase32(value: String): ByteArray {
        val clean = value.trim().uppercase().trimEnd('=')
        var buffer = 0
        var bitsLeft = 0
        val output = ArrayList<Byte>()
        clean.forEach { char ->
            val index = BASE32.indexOf(char)
            require(index >= 0) { "无效的 Base32 密钥" }
            buffer = (buffer shl 5) or index
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.add(((buffer shr (bitsLeft - 8)) and 0xff).toByte())
                bitsLeft -= 8
            }
        }
        return output.toByteArray().also { require(it.isNotEmpty()) }
    }
}
