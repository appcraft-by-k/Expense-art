package com.appcraftbyk.expense.art.data.repository

import androidx.room.withTransaction
import com.appcraftbyk.expense.art.data.local.AppDatabase
import com.appcraftbyk.expense.art.domain.transaction.TransactionRunner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase
) : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T = db.withTransaction(block)
}
