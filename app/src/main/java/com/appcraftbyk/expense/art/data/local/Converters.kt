package com.appcraftbyk.expense.art.data.local

import androidx.room.TypeConverter

/** Stored as the enum name so rows stay readable and stable across versions. */
class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
