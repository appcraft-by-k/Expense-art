package com.appcraftbyk.expense.art.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.appcraftbyk.expense.art.core.money.IndianCurrencyFormatter
import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.DateRanges
import com.appcraftbyk.expense.art.data.security.EncryptedValueStore
import com.appcraftbyk.expense.art.data.security.KeystoreCipher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/** App-wide clock, preference storage, secure value storage, and currency formatting. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private const val PREFERENCES_NAME = "user_prefs"

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideAppClock(clock: Clock): AppClock = AppClock(clock)

    @Provides
    @Singleton
    fun provideDateRanges(appClock: AppClock): DateRanges = DateRanges(appClock)

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { context.preferencesDataStoreFile(PREFERENCES_NAME) }
        )

    @Provides
    @Singleton
    fun provideKeystoreCipher(): KeystoreCipher = KeystoreCipher()

    @Provides
    @Singleton
    fun provideEncryptedValueStore(
        dataStore: DataStore<Preferences>,
        cipher: KeystoreCipher
    ): EncryptedValueStore = EncryptedValueStore(dataStore, cipher)

    @Provides
    @Singleton
    fun provideIndianCurrencyFormatter(): IndianCurrencyFormatter = IndianCurrencyFormatter()
}
