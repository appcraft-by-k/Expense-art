package com.appcraftbyk.expense.art.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeds system categories, savings types, and payment methods with fixed IDs and deterministic
 * uids so their identity survives reinstall, cloud restore, and a future multi-device merge.
 * Only reference data is seeded; no transactions, debts, pots, or budgets.
 *
 * Runs inside Room's `onCreate` transaction. `INSERT OR IGNORE` keeps it idempotent.
 */
object DatabaseSeeder {

    private data class SystemCategory(
        val id: Long,
        val uid: String,
        val name: String,
        val kind: String,
        val iconKey: String,
        val sortOrder: Int
    )

    private data class SystemSavingsType(
        val id: Long,
        val uid: String,
        val name: String,
        val iconKey: String,
        val sortOrder: Int
    )

    private data class SystemPaymentMethod(
        val id: Long,
        val uid: String,
        val name: String,
        val iconKey: String,
        val sortOrder: Int
    )

    // Fixed IDs: expense 1-9, income 10-14 (unique across the shared table).
    private val categories = listOf(
        SystemCategory(1, "sys-category-expense-food", "Food & Dining", "EXPENSE", "food", 0),
        SystemCategory(2, "sys-category-expense-groceries", "Groceries", "EXPENSE", "groceries", 1),
        SystemCategory(3, "sys-category-expense-transport", "Transport", "EXPENSE", "transport", 2),
        SystemCategory(4, "sys-category-expense-utilities", "Utilities", "EXPENSE", "utilities", 3),
        SystemCategory(5, "sys-category-expense-shopping", "Shopping", "EXPENSE", "shopping", 4),
        SystemCategory(6, "sys-category-expense-health", "Health", "EXPENSE", "health", 5),
        SystemCategory(7, "sys-category-expense-entertainment", "Entertainment", "EXPENSE", "entertainment", 6),
        SystemCategory(8, "sys-category-expense-rent", "Rent", "EXPENSE", "rent", 7),
        SystemCategory(9, "sys-category-expense-other", "Other", "EXPENSE", "other", 8),
        SystemCategory(10, "sys-category-income-salary", "Salary", "INCOME", "salary", 0),
        SystemCategory(11, "sys-category-income-freelance", "Freelance", "INCOME", "freelance", 1),
        SystemCategory(12, "sys-category-income-business", "Business", "INCOME", "business", 2),
        SystemCategory(13, "sys-category-income-investments", "Investments", "INCOME", "investments", 3),
        SystemCategory(14, "sys-category-income-other", "Other", "INCOME", "other", 4)
    )

    private val savingsTypes = listOf(
        SystemSavingsType(1, "sys-savings-type-bank-fd", "Bank FD", "bank_fd", 0),
        SystemSavingsType(2, "sys-savings-type-bank-rd", "Bank RD", "bank_rd", 1),
        SystemSavingsType(3, "sys-savings-type-stocks", "Stocks", "stocks", 2),
        SystemSavingsType(4, "sys-savings-type-mutual-fund", "Mutual Fund", "mutual_fund", 3),
        SystemSavingsType(5, "sys-savings-type-cash", "Cash", "cash", 4),
        SystemSavingsType(6, "sys-savings-type-gold", "Gold", "gold", 5),
        SystemSavingsType(7, "sys-savings-type-other", "Other", "other", 6)
    )

    private val paymentMethods = listOf(
        SystemPaymentMethod(1, "sys-payment-method-cash", "Cash", "cash", 0),
        SystemPaymentMethod(2, "sys-payment-method-upi", "UPI", "upi", 1),
        SystemPaymentMethod(3, "sys-payment-method-credit-card", "Credit Card", "credit_card", 2),
        SystemPaymentMethod(4, "sys-payment-method-debit-card", "Debit Card", "debit_card", 3),
        SystemPaymentMethod(5, "sys-payment-method-net-banking", "Net Banking", "net_banking", 4)
    )

    fun seed(db: SupportSQLiteDatabase, now: Long = System.currentTimeMillis()) {
        categories.forEach { category ->
            db.execSQL(
                "INSERT OR IGNORE INTO categories " +
                    "(id, uid, name, kind, iconKey, sortOrder, isSystem, isArchived, createdAt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 1, 0, ?)",
                arrayOf<Any?>(
                    category.id,
                    category.uid,
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
                    "(id, uid, name, iconKey, isSystem, isArchived, sortOrder, createdAt) " +
                    "VALUES (?, ?, ?, ?, 1, 0, ?, ?)",
                arrayOf<Any?>(type.id, type.uid, type.name, type.iconKey, type.sortOrder, now)
            )
        }
        paymentMethods.forEach { method ->
            db.execSQL(
                "INSERT OR IGNORE INTO payment_methods " +
                    "(id, uid, name, iconKey, isSystem, isArchived, sortOrder, createdAt) " +
                    "VALUES (?, ?, ?, ?, 1, 0, ?, ?)",
                arrayOf<Any?>(method.id, method.uid, method.name, method.iconKey, method.sortOrder, now)
            )
        }
    }
}
