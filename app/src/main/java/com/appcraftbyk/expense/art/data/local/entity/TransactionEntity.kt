package com.appcraftbyk.expense.art.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.appcraftbyk.expense.art.core.id.Uids
import com.appcraftbyk.expense.art.domain.model.TransactionKind

/**
 * Categories and payment methods may only be deleted when unused; FK RESTRICT protects history.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["occurredAt"]),
        Index(value = ["categoryId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["kind", "occurredAt"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = Uids.new(),
    val kind: TransactionKind,
    /** Positive minor units (paise); the sign comes from [kind]. */
    val amountMinor: Long,
    val categoryId: Long?,
    /** Epoch millis. */
    val occurredAt: Long,
    val paymentMethodId: Long,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)
