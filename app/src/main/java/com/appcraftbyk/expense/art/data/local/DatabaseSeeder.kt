package com.appcraftbyk.expense.art.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Inserts the built-in catalog. The unique indexes make this safe to run more
 * than once and also merge user data that already uses the same names.
 */
internal fun seedDefaultCatalog(
    db: SupportSQLiteDatabase,
    now: Long = System.currentTimeMillis()
) {
    DefaultCatalog.categories.forEach { category ->
        db.execSQL(
            "INSERT OR IGNORE INTO categories (name, type, is_default, created_at) " +
                "VALUES (?, ?, 1, ?)",
            arrayOf<Any?>(category.name, category.type.name, now)
        )
    }

    DefaultCatalog.paymentMethods.forEach { name ->
        db.execSQL(
            "INSERT OR IGNORE INTO payment_methods (name, is_default, created_at) " +
                "VALUES (?, 1, ?)",
            arrayOf<Any?>(name, now)
        )
    }
}
