package com.appcraftbyk.expense.art.data.mapper

import com.appcraftbyk.expense.art.data.local.entity.CategoryEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtContactEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtEntity
import com.appcraftbyk.expense.art.data.local.entity.DebtRepaymentEntity
import com.appcraftbyk.expense.art.data.local.entity.PaymentMethodEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryEntity
import com.appcraftbyk.expense.art.data.local.entity.SavingsTypeEntity
import com.appcraftbyk.expense.art.data.local.projection.CategorySpend
import com.appcraftbyk.expense.art.data.local.projection.SavingsPotWithBalance
import com.appcraftbyk.expense.art.data.local.projection.TransactionWithCategory
import com.appcraftbyk.expense.art.data.local.projection.UpcomingDue
import com.appcraftbyk.expense.art.domain.model.Category
import com.appcraftbyk.expense.art.domain.model.CategorySpendItem
import com.appcraftbyk.expense.art.domain.model.Debt
import com.appcraftbyk.expense.art.domain.model.DebtContact
import com.appcraftbyk.expense.art.domain.model.DebtRepayment
import com.appcraftbyk.expense.art.domain.model.PaymentMethod
import com.appcraftbyk.expense.art.domain.model.SavingsEntry
import com.appcraftbyk.expense.art.domain.model.SavingsPot
import com.appcraftbyk.expense.art.domain.model.SavingsType
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.UpcomingDueItem
import com.appcraftbyk.expense.art.domain.model.DebtWithContact as DebtWithContactModel

/** Data-layer rows to domain models. Uid and system flags are always carried through. */

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    uid = uid,
    name = name,
    kind = kind,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    uid = uid,
    name = name,
    kind = kind,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = createdAt
)

fun PaymentMethodEntity.toDomain(): PaymentMethod = PaymentMethod(
    id = id,
    uid = uid,
    name = name,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = createdAt
)

fun PaymentMethod.toEntity(): PaymentMethodEntity = PaymentMethodEntity(
    id = id,
    uid = uid,
    name = name,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = createdAt
)

fun SavingsTypeEntity.toDomain(): SavingsType = SavingsType(
    id = id,
    uid = uid,
    name = name,
    iconKey = iconKey,
    isSystem = isSystem,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun SavingsType.toEntity(): SavingsTypeEntity = SavingsTypeEntity(
    id = id,
    uid = uid,
    name = name,
    iconKey = iconKey,
    isSystem = isSystem,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun SavingsPotWithBalance.toDomain(): SavingsPot = SavingsPot(
    id = id,
    uid = uid,
    name = name,
    typeId = typeId,
    typeName = typeName,
    institution = institution,
    targetMinor = targetMinor,
    deadline = deadline,
    balanceMinor = balanceMinor,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun SavingsEntryEntity.toDomain(): SavingsEntry = SavingsEntry(
    id = id,
    uid = uid,
    goalId = goalId,
    kind = kind,
    amountMinor = amountMinor,
    occurredAt = occurredAt,
    note = note
)

fun DebtEntity.toDomain(): Debt = Debt(
    id = id,
    uid = uid,
    contactId = contactId,
    direction = direction,
    principalMinor = principalMinor,
    startDate = startDate,
    dueDate = dueDate,
    note = note,
    status = status,
    settledAt = settledAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.appcraftbyk.expense.art.data.local.projection.DebtWithContact.toDomain(): DebtWithContactModel =
    DebtWithContactModel(
        id = id,
        contactId = contactId,
        contactName = contactName,
        direction = direction,
        principalMinor = principalMinor,
        repaidMinor = repaidMinor,
        startDate = startDate,
        dueDate = dueDate,
        note = note,
        status = status,
        settledAt = settledAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun DebtRepaymentEntity.toDomain(): DebtRepayment = DebtRepayment(
    id = id,
    uid = uid,
    debtId = debtId,
    amountMinor = amountMinor,
    paidAt = paidAt,
    note = note
)

fun DebtContactEntity.toDomain(): DebtContact = DebtContact(
    id = id,
    uid = uid,
    name = name,
    note = note,
    isArchived = isArchived,
    createdAt = createdAt
)

fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = id,
    uid = uid,
    kind = kind,
    amountMinor = amountMinor,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIconKey = categoryIconKey,
    occurredAt = occurredAt,
    paymentMethodId = paymentMethodId,
    paymentMethodName = paymentMethodName,
    note = note
)

fun CategorySpend.toDomain(): CategorySpendItem = CategorySpendItem(
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIconKey = categoryIconKey,
    totalMinor = totalMinor
)

fun UpcomingDue.toDomain(): UpcomingDueItem = UpcomingDueItem(
    debtId = debtId,
    contactName = contactName,
    direction = direction,
    dueMinor = dueMinor,
    dueDate = dueDate
)
