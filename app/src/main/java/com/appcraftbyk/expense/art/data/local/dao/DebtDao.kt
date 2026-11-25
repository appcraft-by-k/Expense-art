package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcraftbyk.expense.art.data.local.entity.DebtContactEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtEntity
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.data.local.entity.DebtRepaymentEntity
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.data.local.projection.DebtWithContact
import com.appcraftbyk.expense.art.data.local.projection.DirectionOutstanding
import com.appcraftbyk.expense.art.data.local.projection.UpcomingDue
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    /** Outstanding is always derived from repayments, grouped by direction and never netted. */
    @Query(
        "SELECT d.direction AS direction, " +
            "COALESCE(SUM(d.principalMinor), 0) - COALESCE(SUM(r.paidMinor), 0) AS outstandingMinor " +
            "FROM debts d " +
            "LEFT JOIN (SELECT debtId, SUM(amountMinor) AS paidMinor FROM debt_repayments GROUP BY debtId) r " +
            "ON r.debtId = d.id " +
            "WHERE d.status = 'OPEN' GROUP BY d.direction"
    )
    fun observeOutstandingByDirection(): Flow<List<DirectionOutstanding>>

    @Query(
        "SELECT d.id AS debtId, c.name AS contactName, d.direction AS direction, " +
            "d.principalMinor - COALESCE(SUM(r.amountMinor), 0) AS dueMinor, d.dueDate AS dueDate " +
            "FROM debts d JOIN debt_contacts c ON c.id = d.contactId " +
            "LEFT JOIN debt_repayments r ON r.debtId = d.id " +
            "WHERE d.status = 'OPEN' AND d.dueDate IS NOT NULL " +
            "AND d.dueDate >= :fromEpochDay AND d.dueDate <= :toEpochDay " +
            "GROUP BY d.id ORDER BY d.dueDate ASC"
    )
    fun observeUpcomingDue(fromEpochDay: Long, toEpochDay: Long): Flow<List<UpcomingDue>>

    @Query(
        "SELECT d.id, d.contactId, c.name AS contactName, d.direction, d.principalMinor, " +
            "COALESCE(SUM(r.amountMinor), 0) AS repaidMinor, d.startDate, d.dueDate, d.note, " +
            "d.status, d.settledAt, d.createdAt, d.updatedAt " +
            "FROM debts d JOIN debt_contacts c ON c.id = d.contactId " +
            "LEFT JOIN debt_repayments r ON r.debtId = d.id " +
            "WHERE :filter = 'ALL' " +
            "OR (:filter = 'I_OWE' AND d.direction = 'BORROWED') " +
            "OR (:filter = 'OWED_TO_ME' AND d.direction = 'LENT') " +
            "OR (:filter = 'OVERDUE' AND d.status = 'OPEN' AND d.dueDate IS NOT NULL AND d.dueDate < :todayEpochDay) " +
            "OR (:filter = 'SETTLED' AND d.status = 'SETTLED') " +
            "GROUP BY d.id " +
            "ORDER BY d.status ASC, COALESCE(d.dueDate, 9223372036854775807) ASC, d.createdAt DESC"
    )
    fun observeDebtsWithContact(filter: DebtListFilter, todayEpochDay: Long): Flow<List<DebtWithContact>>

    @Query(
        "SELECT d.id, d.contactId, c.name AS contactName, d.direction, d.principalMinor, " +
            "COALESCE(SUM(r.amountMinor), 0) AS repaidMinor, d.startDate, d.dueDate, d.note, " +
            "d.status, d.settledAt, d.createdAt, d.updatedAt " +
            "FROM debts d JOIN debt_contacts c ON c.id = d.contactId " +
            "LEFT JOIN debt_repayments r ON r.debtId = d.id " +
            "WHERE d.id = :debtId GROUP BY d.id"
    )
    fun observeDebtWithContact(debtId: Long): Flow<DebtWithContact?>

    @Query("SELECT * FROM debts WHERE id = :debtId")
    suspend fun getDebt(debtId: Long): DebtEntity?

    @Query(
        "SELECT principalMinor - COALESCE((SELECT SUM(amountMinor) FROM debt_repayments WHERE debtId = :debtId), 0) " +
            "FROM debts WHERE id = :debtId"
    )
    suspend fun outstandingMinor(debtId: Long): Long?

    @Query("SELECT * FROM debt_repayments WHERE debtId = :debtId ORDER BY paidAt DESC, id DESC")
    fun observeRepayments(debtId: Long): Flow<List<DebtRepaymentEntity>>

    @Insert
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("UPDATE debts SET status = :status, settledAt = :settledAt, updatedAt = :updatedAt WHERE id = :debtId")
    suspend fun updateStatus(debtId: Long, status: DebtStatus, settledAt: Long?, updatedAt: Long)

    @Insert
    suspend fun insertRepayment(repayment: DebtRepaymentEntity): Long

    @Query("SELECT * FROM debt_contacts ORDER BY isArchived, name")
    fun observeContacts(): Flow<List<DebtContactEntity>>

    @Query("SELECT * FROM debt_contacts WHERE id = :contactId")
    suspend fun getContact(contactId: Long): DebtContactEntity?

    @Query("SELECT COUNT(*) FROM debts WHERE contactId = :contactId")
    suspend fun countDebtsForContact(contactId: Long): Int

    @Insert
    suspend fun insertContact(contact: DebtContactEntity): Long

    @Update
    suspend fun updateContact(contact: DebtContactEntity)
}
