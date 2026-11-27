package com.appcraftbyk.expense.art.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExpenseArtDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "expense_art.db"
    }
}
