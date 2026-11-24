package com.appcraftbyk.expense.art.data.local.entity

/** Sign of a ledger row is carried by this kind; amounts are always stored positive. */
enum class TransactionKind { EXPENSE, INCOME }

enum class PaymentMethod { CASH, UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING }

/** Borrowed ("I Owe") and lent ("Owed to Me") money is never netted. */
enum class DebtDirection { BORROWED, LENT }

enum class DebtStatus { OPEN, SETTLED }

enum class SavingsEntryKind { CONTRIBUTION, WITHDRAWAL }

/** UI-facing debt list filters; resolved in SQL so no full table is loaded. */
enum class DebtListFilter { ALL, I_OWE, OWED_TO_ME, OVERDUE, SETTLED }
