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
    insertCategories(db, now)
    insertPaymentMethods(db, now)
}

/**
 * Seeds only the catalogs that are still empty. Used when the database is opened,
 * so a destructive migration cannot leave the app without categories.
 */
internal fun seedDefaultCatalogIfEmpty(
    db: SupportSQLiteDatabase,
    now: Long = System.currentTimeMillis()
) {
    if (isEmpty(db, "categories")) insertCategories(db, now)
    if (isEmpty(db, "payment_methods")) insertPaymentMethods(db, now)
}

private fun isEmpty(db: SupportSQLiteDatabase, table: String): Boolean =
    db.query("SELECT 1 FROM $table LIMIT 1").use { it.moveToFirst().not() }

private fun insertCategories(db: SupportSQLiteDatabase, now: Long) {
    DefaultCatalog.categories.forEach { category ->
        db.execSQL(
            "INSERT OR IGNORE INTO categories (name, type, is_default, created_at) " +
                "VALUES (?, ?, 1, ?)",
            arrayOf<Any?>(category.name, category.type.name, now)
        )
    }
}

private fun insertPaymentMethods(db: SupportSQLiteDatabase, now: Long) {
    DefaultCatalog.paymentMethods.forEach { name ->
        db.execSQL(
            "INSERT OR IGNORE INTO payment_methods (name, is_default, created_at) " +
                "VALUES (?, 1, ?)",
            arrayOf<Any?>(name, now)
        )
    }
}
