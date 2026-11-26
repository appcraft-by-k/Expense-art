package com.appcraftbyk.expense.art.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.YearMonth

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * User preferences (Plan §10). `profileName` is decrypted PII; everything else is non-sensitive
 * and stored as plaintext so a lost Keystore key cannot lock the user out.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val profileName: String? = null,
    val lastSelectedMonth: YearMonth? = null,
    val onboardingDone: Boolean = false,
    val notificationsEnabled: Boolean = false
)

internal object PreferenceKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val PROFILE_NAME = stringPreferencesKey("profile_name_encrypted")
    val LAST_SELECTED_MONTH = stringPreferencesKey("last_selected_month")
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
}
