package com.appcraftbyk.expense.art.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ExpenseArtDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        const val NAME = "expense_art.db"

        /**
         * Seeds the built-in catalog. Seeding also runs on open because a destructive
         * migration recreates the tables without calling onCreate.
         */
        fun createCallback(): Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                seedDefaultCatalog(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                seedDefaultCatalogIfEmpty(db)
            }
        }
    }
}
