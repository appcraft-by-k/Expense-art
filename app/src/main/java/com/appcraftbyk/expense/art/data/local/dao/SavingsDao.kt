package com.appcraftbyk.expense.art.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsGoalEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsTypeEntity
import com.appcraftbyk.expense.art.data.local.projection.SavingsPotWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Query(
        "SELECT g.id, g.uid, g.name, g.typeId, t.name AS typeName, g.institution, g.targetMinor, g.deadline, " +
            "COALESCE(SUM(CASE WHEN e.kind = 'CONTRIBUTION' THEN e.amountMinor ELSE -e.amountMinor END), 0) AS balanceMinor, " +
            "g.isArchived, g.sortOrder, g.createdAt " +
            "FROM savings_goals g JOIN savings_types t ON t.id = g.typeId " +
            "LEFT JOIN savings_entries e ON e.goalId = g.id " +
            "WHERE (:includeArchived = 1 OR g.isArchived = 0) " +
            "GROUP BY g.id ORDER BY g.isArchived ASC, g.sortOrder ASC, g.createdAt ASC"
    )
    fun observePots(includeArchived: Boolean = false): Flow<List<SavingsPotWithBalance>>

    @Query(
        "SELECT g.id, g.uid, g.name, g.typeId, t.name AS typeName, g.institution, g.targetMinor, g.deadline, " +
            "COALESCE(SUM(CASE WHEN e.kind = 'CONTRIBUTION' THEN e.amountMinor ELSE -e.amountMinor END), 0) AS balanceMinor, " +
            "g.isArchived, g.sortOrder, g.createdAt " +
            "FROM savings_goals g JOIN savings_types t ON t.id = g.typeId " +
            "LEFT JOIN savings_entries e ON e.goalId = g.id " +
            "WHERE g.id = :goalId GROUP BY g.id"
    )
    fun observePot(goalId: Long): Flow<SavingsPotWithBalance?>

    @Query(
        "SELECT g.id, g.uid, g.name, g.typeId, t.name AS typeName, g.institution, g.targetMinor, g.deadline, " +
            "COALESCE(SUM(CASE WHEN e.kind = 'CONTRIBUTION' THEN e.amountMinor ELSE -e.amountMinor END), 0) AS balanceMinor, " +
            "g.isArchived, g.sortOrder, g.createdAt " +
            "FROM savings_goals g JOIN savings_types t ON t.id = g.typeId " +
            "LEFT JOIN savings_entries e ON e.goalId = g.id " +
            "WHERE g.id = :goalId GROUP BY g.id"
    )
    suspend fun getPot(goalId: Long): SavingsPotWithBalance?

    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    suspend fun getGoal(goalId: Long): SavingsGoalEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM savings_goals")
    suspend fun nextGoalSortOrder(): Int

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN kind = 'CONTRIBUTION' THEN amountMinor ELSE -amountMinor END), 0) " +
            "FROM savings_entries WHERE goalId = :goalId"
    )
    suspend fun potBalanceMinor(goalId: Long): Long

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN e.kind = 'CONTRIBUTION' THEN e.amountMinor ELSE -e.amountMinor END), 0) " +
            "FROM savings_entries e JOIN savings_goals g ON g.id = e.goalId WHERE g.isArchived = 0"
    )
    fun observeTotalBalance(): Flow<Long>

    @Query("SELECT COUNT(*) FROM savings_goals WHERE isArchived = 0")
    fun observeActiveGoalCount(): Flow<Int>

    @Query("SELECT * FROM savings_entries WHERE goalId = :goalId ORDER BY occurredAt DESC, id DESC")
    fun observeEntries(goalId: Long): Flow<List<SavingsEntryEntity>>

    @Insert
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Insert
    suspend fun insertEntry(entry: SavingsEntryEntity): Long

    @Query("SELECT * FROM savings_types WHERE (:includeArchived = 1 OR isArchived = 0) ORDER BY sortOrder, name")
    fun observeTypes(includeArchived: Boolean = true): Flow<List<SavingsTypeEntity>>

    @Query("SELECT * FROM savings_types WHERE id = :typeId")
    suspend fun getType(typeId: Long): SavingsTypeEntity?

    @Query("SELECT * FROM savings_types WHERE LOWER(name) = LOWER(:name) AND id != :excludeId LIMIT 1")
    suspend fun findTypeByName(name: String, excludeId: Long = 0): SavingsTypeEntity?

    @Query("SELECT COUNT(*) FROM savings_goals WHERE typeId = :typeId")
    suspend fun countTypeUsage(typeId: Long): Int

    @Insert
    suspend fun insertType(type: SavingsTypeEntity): Long

    @Update
    suspend fun updateType(type: SavingsTypeEntity)

    @Query("UPDATE savings_goals SET typeId = :toTypeId WHERE typeId = :fromTypeId")
    suspend fun reassignGoals(fromTypeId: Long, toTypeId: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM savings_types")
    suspend fun nextTypeSortOrder(): Int

    @Query("DELETE FROM savings_types WHERE id = :typeId")
    suspend fun deleteType(typeId: Long)
}
