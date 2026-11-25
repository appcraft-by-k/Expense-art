package com.appcraftbyk.expense.art.domain.usecase

import com.appcraftbyk.expense.art.core.time.DateRanges
import com.appcraftbyk.expense.art.domain.model.Transaction
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/** Month-scoped ledger rows for one kind; day grouping happens in the screen ViewModel. */
class ObserveLedger @Inject constructor(
    private val transactions: TransactionRepository,
    private val dateRanges: DateRanges
) {

    operator fun invoke(
        kind: TransactionKind,
        yearMonth: YearMonth,
        categoryId: Long? = null
    ): Flow<List<Transaction>> {
        val range = dateRanges.month(yearMonth)
        return transactions.observeRange(
            kind = kind,
            fromMillis = range.fromMillis,
            toMillisExclusive = range.toMillisExclusive,
            categoryId = categoryId
        )
    }
}
