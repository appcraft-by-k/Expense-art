package com.appcraftbyk.expense.art.testing

import com.appcraftbyk.expense.art.domain.model.Category
import com.appcraftbyk.expense.art.domain.model.Debt
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.model.PaymentMethod
import com.appcraftbyk.expense.art.domain.model.SavingsPot
import com.appcraftbyk.expense.art.domain.model.SavingsType
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind

/** Small builders that keep invariant tests readable. */

fun categoryFixture(
    id: Long,
    name: String = "Category $id",
    kind: TransactionKind = TransactionKind.EXPENSE,
    sortOrder: Int = 0,
    isSystem: Boolean = false,
    isArchived: Boolean = false,
    uid: String = "uid-category-$id"
): Category = Category(
    id = id,
    uid = uid,
    name = name,
    kind = kind,
    iconKey = "other",
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = 1L
)

fun paymentMethodFixture(
    id: Long,
    name: String = "Method $id",
    sortOrder: Int = 0,
    isSystem: Boolean = false,
    isArchived: Boolean = false,
    uid: String = "uid-method-$id"
): PaymentMethod = PaymentMethod(
    id = id,
    uid = uid,
    name = name,
    iconKey = "cash",
    sortOrder = sortOrder,
    isSystem = isSystem,
    isArchived = isArchived,
    createdAt = 1L
)

fun savingsTypeFixture(
    id: Long,
    name: String = "Type $id",
    sortOrder: Int = 0,
    isSystem: Boolean = false,
    isArchived: Boolean = false,
    uid: String = "uid-type-$id"
): SavingsType = SavingsType(
    id = id,
    uid = uid,
    name = name,
    iconKey = "other",
    isSystem = isSystem,
    isArchived = isArchived,
    sortOrder = sortOrder,
    createdAt = 1L
)

fun savingsPotFixture(
    id: Long,
    typeId: Long = 1,
    typeName: String = "Type 1",
    name: String = "Pot $id",
    isArchived: Boolean = false,
    uid: String = "uid-pot-$id"
): SavingsPot = SavingsPot(
    id = id,
    uid = uid,
    name = name,
    typeId = typeId,
    typeName = typeName,
    institution = null,
    targetMinor = null,
    deadline = null,
    balanceMinor = 0,
    isArchived = isArchived,
    sortOrder = 0,
    createdAt = 1L
)

fun debtFixture(
    id: Long,
    principalMinor: Long = 10_000,
    direction: DebtDirection = DebtDirection.BORROWED,
    status: DebtStatus = DebtStatus.OPEN,
    contactId: Long = 1,
    uid: String = "uid-debt-$id"
): Debt = Debt(
    id = id,
    uid = uid,
    contactId = contactId,
    direction = direction,
    principalMinor = principalMinor,
    startDate = 20_000,
    dueDate = null,
    note = null,
    status = status,
    settledAt = null,
    createdAt = 1L,
    updatedAt = 1L
)

fun transactionFixture(
    id: Long,
    kind: TransactionKind = TransactionKind.EXPENSE,
    amountMinor: Long = 5_000,
    categoryId: Long? = 1,
    categoryName: String? = "Food & Dining",
    occurredAt: Long = 0
): Transaction = Transaction(
    id = id,
    uid = "uid-transaction-$id",
    kind = kind,
    amountMinor = amountMinor,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryIconKey = "food",
    occurredAt = occurredAt,
    paymentMethodId = 1,
    paymentMethodName = "UPI",
    note = null
)
