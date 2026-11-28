package com.appcraftbyk.expense.art

import android.app.Application
import androidx.room.Room
import com.appcraftbyk.expense.art.data.CategoryRepository
import com.appcraftbyk.expense.art.data.PaymentMethodRepository
import com.appcraftbyk.expense.art.data.TransactionRepository
import com.appcraftbyk.expense.art.data.local.ExpenseArtDatabase

/**
 * Owns the long-lived database/repository singletons for the process.
 * ViewModels should read these through their factory rather than building their own.
 */
class ExpenseArtApplication : Application() {

    val database: ExpenseArtDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            ExpenseArtDatabase::class.java,
            ExpenseArtDatabase.NAME
        )
            // Development-only: recreate the database instead of migrating old schemas.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(ExpenseArtDatabase.createCallback())
            .build()
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val paymentMethodRepository: PaymentMethodRepository by lazy {
        PaymentMethodRepository(database.paymentMethodDao())
    }
}
