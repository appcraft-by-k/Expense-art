package com.appcraftbyk.expense.art.data.local

import androidx.room.TypeConverter
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.DebtListFilter
import com.appcraftbyk.expense.art.domain.model.DebtStatus
import com.appcraftbyk.expense.art.domain.model.SavingsEntryKind
import com.appcraftbyk.expense.art.domain.model.TransactionKind

/** Enums are persisted as their stable TEXT names, never as ordinals. */
class Converters {
    @TypeConverter fun transactionKindToString(value: TransactionKind): String = value.name
    @TypeConverter fun stringToTransactionKind(value: String): TransactionKind = TransactionKind.valueOf(value)

    @TypeConverter fun debtDirectionToString(value: DebtDirection): String = value.name
    @TypeConverter fun stringToDebtDirection(value: String): DebtDirection = DebtDirection.valueOf(value)

    @TypeConverter fun debtStatusToString(value: DebtStatus): String = value.name
    @TypeConverter fun stringToDebtStatus(value: String): DebtStatus = DebtStatus.valueOf(value)

    @TypeConverter fun savingsEntryKindToString(value: SavingsEntryKind): String = value.name
    @TypeConverter fun stringToSavingsEntryKind(value: String): SavingsEntryKind = SavingsEntryKind.valueOf(value)

    @TypeConverter fun debtListFilterToString(value: DebtListFilter): String = value.name
    @TypeConverter fun stringToDebtListFilter(value: String): DebtListFilter = DebtListFilter.valueOf(value)
}
