package com.appcraftbyk.expense.art.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcraftbyk.expense.art.data.local.AppDatabase
import com.appcraftbyk.expense.art.data.local.DatabaseSeeder
import com.appcraftbyk.expense.art.data.local.dao.BudgetDao
import com.appcraftbyk.expense.art.data.local.dao.CategoryDao
import com.appcraftbyk.expense.art.data.local.dao.DebtDao
import com.appcraftbyk.expense.art.data.local.dao.PaymentMethodDao
import com.appcraftbyk.expense.art.data.local.dao.SavingsDao
import com.appcraftbyk.expense.art.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        DatabaseSeeder.seed(db)
                    }
                }
            )
            .build()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideDebtDao(db: AppDatabase): DebtDao = db.debtDao()

    @Provides
    fun provideSavingsDao(db: AppDatabase): SavingsDao = db.savingsDao()

    @Provides
    fun providePaymentMethodDao(db: AppDatabase): PaymentMethodDao = db.paymentMethodDao()
}
