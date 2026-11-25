package com.appcraftbyk.expense.art.data.repository

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.local.dao.SavingsDao
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsGoalEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsTypeEntity
import com.appcraftbyk.expense.art.data.mapper.toDomain
import com.appcraftbyk.expense.art.data.mapper.toEntity
import com.appcraftbyk.expense.art.domain.model.NewSavingsPot
import com.appcraftbyk.expense.art.domain.model.NewSavingsType
import com.appcraftbyk.expense.art.domain.model.SavingsEntry
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.SavingsPot
import com.appcraftbyk.expense.art.domain.model.SavingsType
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavingsRepositoryImpl @Inject constructor(
    private val dao: SavingsDao,
    private val appClock: AppClock
) : SavingsRepository {

    override fun observePots(includeArchived: Boolean): Flow<List<SavingsPot>> =
        dao.observePots(includeArchived).map { rows -> rows.map { it.toDomain() } }

    override fun observePot(goalId: Long): Flow<SavingsPot?> =
        dao.observePot(goalId).map { row -> row?.toDomain() }

    override fun observeEntries(goalId: Long): Flow<List<SavingsEntry>> =
        dao.observeEntries(goalId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getPot(goalId: Long): SavingsPot? = dao.getPot(goalId)?.toDomain()

    override suspend fun potBalanceMinor(goalId: Long): Long = dao.potBalanceMinor(goalId)

    override fun observeTotalBalance(): Flow<Long> = dao.observeTotalBalance()

    override fun observeActiveGoalCount(): Flow<Int> = dao.observeActiveGoalCount()

    override suspend fun nextGoalSortOrder(): Int = dao.nextGoalSortOrder()

    override suspend fun insertGoal(pot: NewSavingsPot): Long = dao.insertGoal(
        SavingsGoalEntity(
            name = pot.name,
            typeId = pot.typeId,
            institution = pot.institution,
            targetMinor = pot.targetMinor,
            deadline = pot.deadline,
            isArchived = false,
            sortOrder = pot.sortOrder,
            createdAt = appClock.nowMillis()
        )
    )

    override suspend fun updateGoal(pot: SavingsPot) = dao.updateGoal(
        SavingsGoalEntity(
            id = pot.id,
            uid = pot.uid,
            name = pot.name,
            typeId = pot.typeId,
            institution = pot.institution,
            targetMinor = pot.targetMinor,
            deadline = pot.deadline,
            isArchived = pot.isArchived,
            sortOrder = pot.sortOrder,
            createdAt = pot.createdAt
        )
    )

    override suspend fun insertEntry(
        goalId: Long,
        kind: SavingsEntryKind,
        amountMinor: Long,
        occurredAt: Long,
        note: String?
    ): Long = dao.insertEntry(
        SavingsEntryEntity(
            goalId = goalId,
            kind = kind,
            amountMinor = amountMinor,
            occurredAt = occurredAt,
            note = note
        )
    )

    override fun observeTypes(includeArchived: Boolean): Flow<List<SavingsType>> =
        dao.observeTypes(includeArchived).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getType(typeId: Long): SavingsType? = dao.getType(typeId)?.toDomain()

    override suspend fun findTypeByName(name: String, excludeId: Long): SavingsType? =
        dao.findTypeByName(name, excludeId)?.toDomain()

    override suspend fun countTypeUsage(typeId: Long): Int = dao.countTypeUsage(typeId)

    override suspend fun nextTypeSortOrder(): Int = dao.nextTypeSortOrder()

    override suspend fun insertType(type: NewSavingsType): Long = dao.insertType(
        SavingsTypeEntity(
            name = type.name,
            iconKey = type.iconKey,
            isSystem = false,
            isArchived = false,
            sortOrder = type.sortOrder,
            createdAt = appClock.nowMillis()
        )
    )

    override suspend fun updateType(type: SavingsType) = dao.updateType(type.toEntity())

    override suspend fun reassignGoals(fromTypeId: Long, toTypeId: Long) =
        dao.reassignGoals(fromTypeId, toTypeId)

    override suspend fun deleteType(typeId: Long) = dao.deleteType(typeId)
}
