package com.appcraftbyk.expense.art.data.local

/** Built-in categories and payment methods inserted on first launch. */
internal object DefaultCatalog {

    data class DefaultCategory(
        val name: String,
        val type: TransactionType
    )

    val categories: List<DefaultCategory> = listOf(
        DefaultCategory("Food & Dining", TransactionType.EXPENSE),
        DefaultCategory("Groceries", TransactionType.EXPENSE),
        DefaultCategory("Transport", TransactionType.EXPENSE),
        DefaultCategory("Utilities", TransactionType.EXPENSE),
        DefaultCategory("Shopping", TransactionType.EXPENSE),
        DefaultCategory("Rent", TransactionType.EXPENSE),
        DefaultCategory("Health", TransactionType.EXPENSE),
        DefaultCategory("Entertainment", TransactionType.EXPENSE),
        DefaultCategory("Education", TransactionType.EXPENSE),
        DefaultCategory("Travel", TransactionType.EXPENSE),
        DefaultCategory("Other", TransactionType.EXPENSE),
        DefaultCategory("Salary", TransactionType.INCOME),
        DefaultCategory("Freelance", TransactionType.INCOME),
        DefaultCategory("Business", TransactionType.INCOME),
        DefaultCategory("Investments", TransactionType.INCOME),
        DefaultCategory("Gift", TransactionType.INCOME),
        DefaultCategory("Other", TransactionType.INCOME)
    )

    val paymentMethods: List<String> = listOf(
        "Cash",
        "UPI",
        "GPay",
        "Debit Card",
        "Credit Card",
        "Net Banking",
        "Wallet",
        "Other"
    )
}
