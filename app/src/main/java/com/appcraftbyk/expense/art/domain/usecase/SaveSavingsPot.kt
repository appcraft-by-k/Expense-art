package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewSavingsPot
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import javax.inject.Inject

/** Creates or edits a savings pot. The balance is always derived from entries, never supplied. */
class SaveSavingsPot @Inject constructor(
    private val savings: SavingsRepository,
    private val appClock: AppClock
) {

    suspend operator fun invoke(
        potId: Long?,
        name: String,
        typeId: Long,
        institution: String? = null,
        targetMinor: Long? = null,
        deadline: Long? = null,
        isArchived: Boolean? = null
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) throw DomainException.BlankName
        if (targetMinor != null && targetMinor <= 0) throw DomainException.InvalidAmount

        val type = savings.getType(typeId) ?: throw DomainException.NotFound
        val existing = potId?.let { savings.getPot(it) ?: throw DomainException.NotFound }

        val changingType = existing == null || existing.typeId != typeId
        if (changingType && type.isArchived) throw DomainException.ArchivedTarget

        val cleanInstitution = institution?.trim()?.takeIf { it.isNotEmpty() }

        if (existing == null) {
            savings.insertGoal(
                NewSavingsPot(
                    name = cleanName,
                    typeId = typeId,
                    institution = cleanInstitution,
                    targetMinor = targetMinor,
                    deadline = deadline,
                    sortOrder = savings.nextGoalSortOrder()
                )
            )
        } else {
            savings.updateGoal(
                existing.copy(
                    name = cleanName,
                    typeId = typeId,
                    institution = cleanInstitution,
                    targetMinor = targetMinor,
                    deadline = deadline,
                    isArchived = isArchived ?: existing.isArchived
                )
            )
        }
    }
}
