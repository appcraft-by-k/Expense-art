package com.appcraftbyk.expense.art.domain.transaction

/**
 * Runs a multi-step invariant inside one database transaction. Use cases depend on this
 * interface so their rules stay testable with an in-memory fake.
 */
interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T
}
