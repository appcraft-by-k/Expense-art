package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewSavingsType
import com.appcraftbyk.expense.art.domain.repository.SavingsRepository
import javax.inject.Inject

/**
 * Creates or edits a savings type. Seeded types keep their uid and system flag; names stay unique
 * case-insensitively.
 */
class SaveSavingsType @Inject constructor(
    private val savings: SavingsRepository
) {

    suspend operator fun invoke(
        typeId: Long?,
        name: String,
        iconKey: String,
        sortOrder: Int? = null,
        isArchived: Boolean? = null
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) throw DomainException.BlankName

        val existing = typeId?.let { savings.getType(it) ?: throw DomainException.NotFound }
        val duplicate = savings.findTypeByName(cleanName, excludeId = typeId ?: 0)
        if (duplicate != null) throw DomainException.DuplicateName(cleanName)

        if (existing == null) {
            savings.insertType(
                NewSavingsType(
                    name = cleanName,
                    iconKey = iconKey,
                    sortOrder = sortOrder ?: savings.nextTypeSortOrder()
                )
            )
        } else {
            savings.updateType(
                existing.copy(
                    name = cleanName,
                    iconKey = iconKey,
                    sortOrder = sortOrder ?: existing.sortOrder,
                    isArchived = isArchived ?: existing.isArchived
                )
            )
        }
    }
}
