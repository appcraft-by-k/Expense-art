package com.appcraftbyk.expense.art.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeds system categories and savings types with fixed IDs so their identity survives
 * reinstall and cloud restore. Only reference data is seeded; no transactions, debts,
 * pots, or budgets.
 *
 * Runs inside Room's `onCreate` transaction. `INSERT OR IGNORE` keeps it idempotent.
 */
object DatabaseSeeder {

    private data class SystemCategory(
        val id: Long,
        val name: String,
        val kind: String,
        val iconKey: String,
        val sortOrder: Int
    )

    private data class SystemSavingsType(
        val id: Long,
        val name: String,
        val iconKey: String,
        val sortOrder: Int
    )

    // Fixed IDs: expense 1-9, income 10-14 (unique across the shared table).
    private val categories = listOf(
        SystemCategory(1, "Food & Dining", "EXPENSE", "food", 0),
        SystemCategory(2, "Groceries", "EXPENSE", "groceries", 1),
        SystemCategory(3, "Transport", "EXPENSE", "transport", 2),
        SystemCategory(4, "Utilities", "EXPENSE", "utilities", 3),
        SystemCategory(5, "Shopping", "EXPENSE", "shopping", 4),
        SystemCategory(6, "Health", "EXPENSE", "health", 5),
        SystemCategory(7, "Entertainment", "EXPENSE", "entertainment", 6),
        SystemCategory(8, "Rent", "EXPENSE", "rent", 7),
        SystemCategory(9, "Other", "EXPENSE", "other", 8),
        SystemCategory(10, "Salary", "INCOME", "salary", 0),
        SystemCategory(11, "Freelance", "INCOME", "freelance", 1),
        SystemCategory(12, "Business", "INCOME", "business", 2),
        SystemCategory(13, "Investments", "INCOME", "investments", 3),
        SystemCategory(14, "Other", "INCOME", "other", 4)
    )

    private val savingsTypes = listOf(
        SystemSavingsType(1, "Bank FD", "bank_fd", 0),
        SystemSavingsType(2, "Bank RD", "bank_rd", 1),
        SystemSavingsType(3, "Stocks", "stocks", 2),
        SystemSavingsType(4, "Mutual Fund", "mutual_fund", 3),
        SystemSavingsType(5, "Cash", "cash", 4),
        SystemSavingsType(6, "Gold", "gold", 5),
        SystemSavingsType(7, "Other", "other", 6)
    )

    fun seed(db: SupportSQLiteDatabase, now: Long = System.currentTimeMillis()) {
        categories.forEach { category ->
            db.execSQL(
                "INSERT OR IGNORE INTO categories " +
                    "(id, name, kind, iconKey, sortOrder, isSystem, isArchived, createdAt) " +
                    "VALUES (?, ?, ?, ?, ?, 1, 0, ?)",
                arrayOf<Any?>(
                    category.id,
                    category.name,
                    category.kind,
                    category.iconKey,
                    category.sortOrder,
                    now
                )
            )
        }
        savingsTypes.forEach { type ->
            db.execSQL(
                "INSERT OR IGNORE INTO savings_types " +
                    "(id, name, iconKey, isSystem, isArchived, sortOrder, createdAt) " +
                    "VALUES (?, ?, ?, 1, 0, ?, ?)",
                arrayOf<Any?>(type.id, type.name, type.iconKey, type.sortOrder, now)
            )
        }
    }
}
