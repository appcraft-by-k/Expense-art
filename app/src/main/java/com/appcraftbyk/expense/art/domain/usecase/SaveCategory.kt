package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewCategory
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.CategoryRepository
import javax.inject.Inject

/**
 * Creates or edits a category. Names are unique case-insensitively per kind; system rows keep
 * their uid and system flag, and may only be archived (never deleted).
 */
class SaveCategory @Inject constructor(
    private val categories: CategoryRepository
) {

    suspend operator fun invoke(
        categoryId: Long?,
        kind: TransactionKind,
        name: String,
        iconKey: String,
        sortOrder: Int? = null,
        isArchived: Boolean? = null
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) throw DomainException.BlankName

        val existing = categoryId?.let { categories.get(it) ?: throw DomainException.NotFound }
        if (existing != null && existing.kind != kind) throw DomainException.CategoryKindMismatch

        val duplicate = categories.findByName(kind, cleanName, excludeId = categoryId ?: 0)
        if (duplicate != null) throw DomainException.DuplicateName(cleanName)

        if (existing == null) {
            categories.insert(
                NewCategory(
                    name = cleanName,
                    kind = kind,
                    iconKey = iconKey,
                    sortOrder = sortOrder ?: categories.nextSortOrder(kind)
                )
            )
        } else {
            categories.update(
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
