package com.appcraftbyk.expense.art.di

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.core.time.DateRanges
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * App-wide clock and calendar helpers. Preferences, Keystore storage, and the currency formatter
 * are wired in P3 alongside the settings/theme work.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideAppClock(clock: Clock): AppClock = AppClock(clock)

    @Provides
    @Singleton
    fun provideDateRanges(appClock: AppClock): DateRanges = DateRanges(appClock)
}
