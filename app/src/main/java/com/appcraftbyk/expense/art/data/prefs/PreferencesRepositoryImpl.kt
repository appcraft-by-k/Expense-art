package com.appcraftbyk.expense.art.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.appcraftbyk.expense.art.data.security.EncryptedValueStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.YearMonth
import java.time.format.DateTimeParseException
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val encryptedValueStore: EncryptedValueStore
) : PreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { prefs ->
            val storedName = prefs[PreferenceKeys.PROFILE_NAME]
            val profileName = storedName?.let(encryptedValueStore::decryptOrNull)
            if (storedName != null && profileName == null) {
                // Key lost or payload corrupted: drop the value so it cannot fail again.
                encryptedValueStore.clear(PreferenceKeys.PROFILE_NAME)
            }
            prefs.toUserPreferences(profileName)
        }
        .flowOn(Dispatchers.IO)

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setProfileName(name: String?) {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            encryptedValueStore.clear(PreferenceKeys.PROFILE_NAME)
        } else {
            encryptedValueStore.put(PreferenceKeys.PROFILE_NAME, trimmed)
        }
    }

    override suspend fun setLastSelectedMonth(yearMonth: YearMonth) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LAST_SELECTED_MONTH] = yearMonth.toString()
        }
    }

    private fun Preferences.toUserPreferences(profileName: String?): UserPreferences =
        UserPreferences(
            themeMode = this[PreferenceKeys.THEME_MODE]
                ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: ThemeMode.SYSTEM,
            profileName = profileName,
            lastSelectedMonth = this[PreferenceKeys.LAST_SELECTED_MONTH]?.let(::parseYearMonth),
            onboardingDone = this[PreferenceKeys.ONBOARDING_DONE] ?: false,
            notificationsEnabled = this[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: false
        )

    private fun parseYearMonth(value: String): YearMonth? =
        try {
            YearMonth.parse(value)
        } catch (e: DateTimeParseException) {
            null
        }
}
