package com.appcraftbyk.expense.art.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcraftbyk.expense.art.data.local.dao.BudgetDao
import com.appcraftbyk.expense.art.data.local.dao.CategoryDao
import com.appcraftbyk.expense.art.data.local.dao.DebtDao
import com.appcraftbyk.expense.art.data.local.dao.SavingsDao
import com.appcraftbyk.expense.art.data.local.dao.TransactionDao
import com.appcraftbyk.expense.art.data.local.entity.CategoryEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtContactEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtRepaymentEntity
import com.appcraftbyk.expense.art.data.local.entity.MonthlyBudgetEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsGoalEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsTypeEntity
import com.appcraftbyk.expense.art.data.local.entity.TransactionEntity

/**
 * Schema version 1. Schema JSON is exported to `app/schemas` and must be committed because the
 * v1 -> v2 reminder migration depends on it.
 *
 * Never enable destructive migration: user data must survive every schema change.
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        MonthlyBudgetEntity::class,
        DebtContactEntity::class,
        DebtEntity::class,
        DebtRepaymentEntity::class,
        SavingsTypeEntity::class,
        SavingsGoalEntity::class,
        SavingsEntryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun savingsDao(): SavingsDao

    companion object {
        const val NAME = "expense_art.db"
    }
}
