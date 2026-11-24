package com.appcraftbyk.expense.art.data.local

import androidx.room.TypeConverter
import com.appcraftbyk.expense.art.data.local.entity.DebtDirection
import com.appcraftbyk.expense.art.data.local.entity.DebtListFilter
import com.appcraftbyk.expense.art.data.local.entity.DebtStatus
import com.appcraftbyk.expense.art.data.local.entity.PaymentMethod
import com.appcraftbyk.expense.art.data.local.entity.SavingsEntryKind
import com.appcraftbyk.expense.art.data.local.entity.TransactionKind

/** Enums are persisted as their stable TEXT names, never as ordinals. */
class Converters {
    @TypeConverter fun transactionKindToString(value: TransactionKind): String = value.name
    @TypeConverter fun stringToTransactionKind(value: String): TransactionKind = TransactionKind.valueOf(value)

    @TypeConverter fun paymentMethodToString(value: PaymentMethod): String = value.name
    @TypeConverter fun stringToPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter fun debtDirectionToString(value: DebtDirection): String = value.name
    @TypeConverter fun stringToDebtDirection(value: String): DebtDirection = DebtDirection.valueOf(value)

    @TypeConverter fun debtStatusToString(value: DebtStatus): String = value.name
    @TypeConverter fun stringToDebtStatus(value: String): DebtStatus = DebtStatus.valueOf(value)

    @TypeConverter fun savingsEntryKindToString(value: SavingsEntryKind): String = value.name
    @TypeConverter fun stringToSavingsEntryKind(value: String): SavingsEntryKind = SavingsEntryKind.valueOf(value)

    @TypeConverter fun debtListFilterToString(value: DebtListFilter): String = value.name
    @TypeConverter fun stringToDebtListFilter(value: String): DebtListFilter = DebtListFilter.valueOf(value)
}
