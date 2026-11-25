package com.appcraftbyk.expense.art.domain.repository

import com.appcraftbyk.expense.art.domain.model.NewSavingsPot
import com.appcraftbyk.expense.art.domain.model.NewSavingsType
import com.appcraftbyk.expense.art.domain.model.SavingsEntry
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.SavingsPot
import com.appcraftbyk.expense.art.domain.model.SavingsType
import kotlinx.coroutines.flow.Flow

interface SavingsRepository {

    fun observePots(includeArchived: Boolean = false): Flow<List<SavingsPot>>

    fun observePot(goalId: Long): Flow<SavingsPot?>

    fun observeEntries(goalId: Long): Flow<List<SavingsEntry>>

    suspend fun getPot(goalId: Long): SavingsPot?

    suspend fun potBalanceMinor(goalId: Long): Long

    fun observeTotalBalance(): Flow<Long>

    fun observeActiveGoalCount(): Flow<Int>

    suspend fun nextGoalSortOrder(): Int

    suspend fun insertGoal(pot: NewSavingsPot): Long

    /** Updates pot metadata only; the balance is always derived from entries. */
    suspend fun updateGoal(pot: SavingsPot)

    suspend fun insertEntry(
        goalId: Long,
        kind: SavingsEntryKind,
        amountMinor: Long,
        occurredAt: Long,
        note: String?
    ): Long

    fun observeTypes(includeArchived: Boolean = true): Flow<List<SavingsType>>

    suspend fun getType(typeId: Long): SavingsType?

    /** Case-insensitive duplicate check; [excludeId] lets a rename keep its own name. */
    suspend fun findTypeByName(name: String, excludeId: Long = 0): SavingsType?

    suspend fun countTypeUsage(typeId: Long): Int

    suspend fun nextTypeSortOrder(): Int

    suspend fun insertType(type: NewSavingsType): Long

    suspend fun updateType(type: SavingsType)

    suspend fun reassignGoals(fromTypeId: Long, toTypeId: Long)

    suspend fun deleteType(typeId: Long)
}
