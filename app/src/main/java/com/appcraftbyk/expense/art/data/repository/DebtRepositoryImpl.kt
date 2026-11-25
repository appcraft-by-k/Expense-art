package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.data.local.dao.DebtDao
import com.appcraftbyk.expense.art.data.local.entity.DebtRepaymentEntity
import com.appcraftbyk.expense.art.data.mapper.toDomain
import com.appcraftbyk.expense.art.domain.model.Debt
import com.appcraftbyk.expense.art.domain.model.DebtContact
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.domain.model.DebtRepayment
import com.appcraftbyk.expense.art.domain.model.DebtWithContact
import com.appcraftbyk.expense.art.domain.model.UpcomingDueItem
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepositoryImpl @Inject constructor(
    private val dao: DebtDao
) : DebtRepository {

    override fun observeOutstandingByDirection(): Flow<Map<DebtDirection, Long>> =
        dao.observeOutstandingByDirection()
            .map { rows -> rows.associate { it.direction to it.outstandingMinor } }

    override fun observeUpcomingDue(
        fromEpochDay: Long,
        toEpochDay: Long
    ): Flow<List<UpcomingDueItem>> =
        dao.observeUpcomingDue(fromEpochDay, toEpochDay)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeDebts(
        filter: DebtListFilter,
        todayEpochDay: Long
    ): Flow<List<DebtWithContact>> =
        dao.observeDebtsWithContact(filter, todayEpochDay)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeDebtWithContact(debtId: Long): Flow<DebtWithContact?> =
        dao.observeDebtWithContact(debtId).map { row -> row?.toDomain() }

    override suspend fun getDebt(debtId: Long): Debt? = dao.getDebt(debtId)?.toDomain()

    override suspend fun outstandingMinor(debtId: Long): Long? = dao.outstandingMinor(debtId)

    override fun observeRepayments(debtId: Long): Flow<List<DebtRepayment>> =
        dao.observeRepayments(debtId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun insertRepayment(
        debtId: Long,
        amountMinor: Long,
        paidAt: Long,
        note: String?
    ): Long = dao.insertRepayment(
        DebtRepaymentEntity(
            debtId = debtId,
            amountMinor = amountMinor,
            paidAt = paidAt,
            note = note
        )
    )

    override suspend fun markSettled(debtId: Long, settledAt: Long, updatedAt: Long) =
        dao.updateStatus(debtId, DebtStatus.SETTLED, settledAt, updatedAt)

    override fun observeContacts(): Flow<List<DebtContact>> =
        dao.observeContacts().map { rows -> rows.map { it.toDomain() } }
}
