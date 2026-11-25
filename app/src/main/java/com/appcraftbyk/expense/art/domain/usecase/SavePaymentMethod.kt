package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.domain.error.DomainException
import com.appcraftbyk.expense.art.domain.model.NewPaymentMethod
import com.appcraftbyk.expense.art.domain.repository.PaymentMethodRepository
import javax.inject.Inject

/**
 * Creates or edits a payment method. Seeded methods keep their uid and system flag; names stay
 * unique case-insensitively.
 */
class SavePaymentMethod @Inject constructor(
    private val paymentMethods: PaymentMethodRepository
) {

    suspend operator fun invoke(
        paymentMethodId: Long?,
        name: String,
        iconKey: String,
        sortOrder: Int? = null,
        isArchived: Boolean? = null
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) throw DomainException.BlankName

        val existing = paymentMethodId?.let {
            paymentMethods.get(it) ?: throw DomainException.NotFound
        }
        val duplicate = paymentMethods.findByName(cleanName, excludeId = paymentMethodId ?: 0)
        if (duplicate != null) throw DomainException.DuplicateName(cleanName)

        if (existing == null) {
            paymentMethods.insert(
                NewPaymentMethod(
                    name = cleanName,
                    iconKey = iconKey,
                    sortOrder = sortOrder ?: paymentMethods.nextSortOrder()
                )
            )
        } else {
            paymentMethods.update(
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
