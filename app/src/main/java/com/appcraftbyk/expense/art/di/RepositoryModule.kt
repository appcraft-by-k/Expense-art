package com.appcraftbyk.expense.art.di

import com.appcraftbyk.expense.art.data.prefs.PreferencesRepository
import com.appcraftbyk.expense.art.data.prefs.PreferencesRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.BudgetRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.CategoryRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.DebtRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.PaymentMethodRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.RoomTransactionRunner
import com.appcraftbyk.expense.art.data.repository.SavingsRepositoryImpl
import com.appcraftbyk.expense.art.data.repository.TransactionRepositoryImpl
import com.appcraftbyk.expense.art.domain.repository.BudgetRepository
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(
        impl: PaymentMethodRepositoryImpl
    ): PaymentMethodRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository

    @Binds
    @Singleton
    abstract fun bindSavingsRepository(impl: SavingsRepositoryImpl): SavingsRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRunner(impl: RoomTransactionRunner): TransactionRunner

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
