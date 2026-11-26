package com.appcraftbyk.expense.art.data.prefs

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/** Reactive access to user preferences; all writes are suspending and backed by DataStore. */
interface PreferencesRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    /** Pass `null` or a blank string to remove the profile name. */
    suspend fun setProfileName(name: String?)

    suspend fun setLastSelectedMonth(yearMonth: YearMonth)
}
