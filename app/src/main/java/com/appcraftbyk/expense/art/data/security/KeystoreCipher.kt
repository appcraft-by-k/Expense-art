package com.appcraftbyk.expense.art.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256/GCM cipher backed by an Android Keystore key (Plan §10).
 *
 * The key is created on first write and never leaves the Keystore. Decryption never creates a key:
 * if the key is gone (restored device, cleared Keystore) the value is treated as absent instead of
 * being silently re-encrypted with a different key.
 *
 * Callers must never log plaintext or the decrypted content.
 */
class KeystoreCipher {

    /** Returns `iv || ciphertext`, which is what [decrypt] expects. */
    fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return cipher.iv + ciphertext
    }

    /** Returns `null` for a missing key, a malformed payload, or an authentication failure. */
    fun decrypt(payload: ByteArray): String? {
        if (payload.size <= IV_SIZE_BYTES) return null
        return try {
            val key = existingKey() ?: return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(TAG_SIZE_BITS, payload, 0, IV_SIZE_BYTES)
            )
            String(
                cipher.doFinal(payload, IV_SIZE_BYTES, payload.size - IV_SIZE_BYTES),
                Charsets.UTF_8
            )
        } catch (e: GeneralSecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    private fun existingKey(): SecretKey? =
        (keyStore().getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        existingKey()?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "expense_art_pii"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val TAG_SIZE_BITS = 128
        const val IV_SIZE_BYTES = 12
    }
}
