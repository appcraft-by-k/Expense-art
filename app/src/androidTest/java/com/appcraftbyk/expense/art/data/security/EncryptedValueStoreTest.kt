package com.appcraftbyk.expense.art.data.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EncryptedValueStoreTest {

    private val key = stringPreferencesKey("secret")
    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: EncryptedValueStore

    @Before
    fun setUp() {
        file = File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "encrypted_store_test_${System.nanoTime()}.preferences_pb"
        )
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        store = EncryptedValueStore(dataStore, KeystoreCipher())
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
    }

    @Test
    fun roundTripsThroughKeystoreEncryption() = runBlocking {
        store.put(key, "Asha")

        val stored = dataStore.data.first()[key]
        assertFalse(stored!!.contains("Asha"))
        assertEquals("Asha", store.decryptOrNull(stored))
    }

    @Test
    fun malformedPayloadDecryptsToNull() {
        assertNull(store.decryptOrNull("not-base64!!"))
    }

    @Test
    fun payloadWithWrongKeyFailsClosed() {
        // A syntactically valid Base64 payload that is not `iv || ciphertext` for this key.
        assertNull(store.decryptOrNull("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))
    }
}
