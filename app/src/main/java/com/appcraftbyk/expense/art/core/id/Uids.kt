package com.appcraftbyk.expense.art.core.id

import java.util.UUID

/**
 * Stable row identity for cloud backup/merge. Local integer primary keys stay the storage
 * key; [new] is only used when the repository creates a user row.
 */
object Uids {
    fun new(): String = UUID.randomUUID().toString()
}
