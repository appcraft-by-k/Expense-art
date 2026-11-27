package com.appcraftbyk.expense.art

import android.app.Application
import androidx.room.Room
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
        ).build()
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }
}
