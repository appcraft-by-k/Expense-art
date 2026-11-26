package com.appcraftbyk.expense.art.data.security

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

/**
 * Persists individual string preferences encrypted as Base64(`iv || ciphertext`).
 *
 * A value that cannot be decoded or decrypted (restored device, rotated Keystore key) is treated
 * as absent and removed, so a lost key can never crash or lock the user out of the app.
 */
class EncryptedValueStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: KeystoreCipher
) {

    suspend fun put(key: Preferences.Key<String>, value: String) {
        val encoded = Base64.encodeToString(cipher.encrypt(value), Base64.NO_WRAP)
        dataStore.edit { prefs -> prefs[key] = encoded }
    }

    suspend fun clear(key: Preferences.Key<String>) {
        dataStore.edit { prefs -> prefs.remove(key) }
    }

    /** Decrypts a stored value, or returns `null` when it is malformed or unreadable. */
    fun decryptOrNull(encoded: String): String? {
        val payload = try {
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return cipher.decrypt(payload)
    }
}
