package com.bombless.authenticator.data

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EntryStore(context: Context) {
    private val prefs = context.getSharedPreferences("vault", Context.MODE_PRIVATE)
    private val alias = "authenticator.entries.v1"

    fun load(): List<OtpEntry> = runCatching {
        val packed = prefs.getString("entries", null) ?: return emptyList()
        val bytes = Base64.decode(packed, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, 12)
        val encrypted = bytes.copyOfRange(12, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        val array = JSONArray(String(cipher.doFinal(encrypted), Charsets.UTF_8))
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            OtpEntry(
                id = item.getString("id"), issuer = item.getString("issuer"),
                account = item.getString("account"), secret = item.getString("secret"),
                algorithm = item.optString("algorithm", "SHA1"),
                digits = item.optInt("digits", 6), period = item.optInt("period", 30)
            )
        }
    }.getOrDefault(emptyList())

    fun save(entries: List<OtpEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().put("id", entry.id).put("issuer", entry.issuer)
                .put("account", entry.account).put("secret", entry.secret)
                .put("algorithm", entry.algorithm).put("digits", entry.digits).put("period", entry.period))
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
        prefs.edit().putString("entries", Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance("AES", "AndroidKeyStore").run {
            init(android.security.keystore.KeyGenParameterSpec.Builder(
                alias, android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
            generateKey()
        }
    }
}
