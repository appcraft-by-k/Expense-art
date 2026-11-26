package com.appcraftbyk.expense.art.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcraftbyk.expense.art.core.money.AmountParser
import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.prefs.PreferencesRepository
import com.appcraftbyk.expense.art.domain.model.DebtDirection
import com.appcraftbyk.expense.art.domain.model.HomeSummary
import com.appcraftbyk.expense.art.domain.model.TransactionKind
import com.appcraftbyk.expense.art.domain.usecase.ObserveHomeSummary
import com.appcraftbyk.expense.art.domain.usecase.SetBudget
import com.appcraftbyk.expense.art.ui.common.LoadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

data class HomeCategoryRow(
    /** `null` when the transaction has no category. */
    val name: String?,
    val amountMinor: Long
)

data class HomeDueRow(
    val debtId: Long,
    val contactName: String,
    val direction: DebtDirection,
    val amountMinor: Long,
    val dueEpochDay: Long,
    /** Negative when overdue. */
    val daysUntilDue: Long
)

data class HomeTransactionRow(
    val id: Long,
    val kind: TransactionKind,
    /** Note when present, otherwise the category name. */
    val title: String?,
    val categoryName: String?,
    val categoryIconKey: String?,
    val amountMinor: Long,
    val occurredAt: Long
)

enum class HomeError { LOAD_FAILED }

enum class BudgetEditorError { INVALID_AMOUNT, SAVE_FAILED }

data class BudgetEditorState(
    val isOpen: Boolean = false,
    val input: String = "",
    val hasExistingBudget: Boolean = false,
    val isSaving: Boolean = false,
    val error: BudgetEditorError? = null
)

data class HomeUiState(
    val status: LoadStatus,
    val period: YearMonth,
    val greetingName: String? = null,
    val incomeMinor: Long = 0L,
    val expenseMinor: Long = 0L,
    val incomeDeltaPercent: Int? = null,
    val expenseShareOfIncomePercent: Int? = null,
    val oweMinor: Long = 0L,
    val owedToMeMinor: Long = 0L,
    val totalSavingsMinor: Long = 0L,
    val activeGoalCount: Int = 0,
    val budgetLimitMinor: Long? = null,
    val budgetHealth: BudgetHealth? = null,
    val budgetUsedFraction: Float = 0f,
    val topCategories: List<HomeCategoryRow> = emptyList(),
    /** Spend not covered by [topCategories]; rendered as "Others". */
    val otherSpendMinor: Long = 0L,
    val upcomingDues: List<HomeDueRow> = emptyList(),
    val recentTransactions: List<HomeTransactionRow> = emptyList(),
    val error: HomeError? = null
) {
    /** A month with no transactions and no budget shows the empty state instead of a zeros-only grid. */
    val isMonthEmpty: Boolean
        get() = incomeMinor == 0L && expenseMinor == 0L && budgetLimitMinor == null

    val budgetRemainingMinor: Long?
        get() = budgetLimitMinor?.let { it - expenseMinor }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeHomeSummary: ObserveHomeSummary,
    private val setBudget: SetBudget,
    private val preferencesRepository: PreferencesRepository,
    private val appClock: AppClock
) : ViewModel() {

    private val period = MutableStateFlow(appClock.currentYearMonth())
    private val retryTrigger = MutableStateFlow(0)
    private val _budgetEditor = MutableStateFlow(BudgetEditorState())
    val budgetEditor: StateFlow<BudgetEditorState> = _budgetEditor.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        period,
        retryTrigger,
        preferencesRepository.userPreferences
    ) { currentPeriod, _, prefs ->
        currentPeriod to prefs.profileName
    }.flatMapLatest { (currentPeriod, profileName) ->
        observeHomeSummary(currentPeriod)
            .map { summary -> summary.toUiState(profileName) }
            .catch {
                emit(
                    HomeUiState(
                        status = LoadStatus.Error,
                        period = currentPeriod,
                        greetingName = profileName,
                        error = HomeError.LOAD_FAILED
                    )
                )
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(status = LoadStatus.Loading, period = appClock.currentYearMonth())
    )

    /** The selected month is owned by `MainViewModel`; the Home screen mirrors it here. */
    fun setPeriod(yearMonth: YearMonth) {
        period.value = yearMonth
    }

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    fun openBudgetEditor() {
        val existing = uiState.value.budgetLimitMinor
        _budgetEditor.value = BudgetEditorState(
            isOpen = true,
            input = existing?.let(::minorToInput).orEmpty(),
            hasExistingBudget = existing != null
        )
    }

    fun dismissBudgetEditor() {
        _budgetEditor.value = BudgetEditorState()
    }

    fun onBudgetInputChange(value: String) {
        _budgetEditor.update {
            it.copy(input = value.take(MAX_BUDGET_INPUT_LENGTH), error = null)
        }
    }

    fun saveBudget() {
        val amountMinor = AmountParser.parseRupeesToMinor(_budgetEditor.value.input)
        if (amountMinor == null || amountMinor <= 0L) {
            _budgetEditor.update { it.copy(error = BudgetEditorError.INVALID_AMOUNT) }
            return
        }
        writeBudget(amountMinor)
    }

    fun removeBudget() {
        writeBudget(null)
    }

    private fun writeBudget(amountMinor: Long?) {
        viewModelScope.launch {
            _budgetEditor.update { it.copy(isSaving = true, error = null) }
            try {
                setBudget(period.value, amountMinor)
                _budgetEditor.value = BudgetEditorState()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _budgetEditor.update {
                    it.copy(isSaving = false, error = BudgetEditorError.SAVE_FAILED)
                }
            }
        }
    }

    private fun HomeSummary.toUiState(profileName: String?): HomeUiState {
        val topSum = topCategories.sumOf { it.totalMinor }
        val today = appClock.todayEpochDay()
        return HomeUiState(
            status = if (incomeMinor == 0L && expenseMinor == 0L && budgetLimitMinor == null) {
                LoadStatus.Empty
            } else {
                LoadStatus.Content
            },
            period = period,
            greetingName = profileName?.trim()?.takeIf { it.isNotEmpty() },
            incomeMinor = incomeMinor,
            expenseMinor = expenseMinor,
            incomeDeltaPercent = HomeMetrics.incomeDeltaPercent(incomeMinor, previousIncomeMinor),
            expenseShareOfIncomePercent = HomeMetrics.expenseShareOfIncomePercent(
                expenseMinor,
                incomeMinor
            ),
            oweMinor = oweMinor,
            owedToMeMinor = owedToMeMinor,
            totalSavingsMinor = totalSavingsMinor,
            activeGoalCount = activeGoalCount,
            budgetLimitMinor = budgetLimitMinor,
            budgetHealth = HomeMetrics.budgetHealth(expenseMinor, budgetLimitMinor),
            budgetUsedFraction = HomeMetrics.budgetUsedFraction(expenseMinor, budgetLimitMinor),
            topCategories = topCategories.map {
                HomeCategoryRow(name = it.categoryName, amountMinor = it.totalMinor)
            },
            otherSpendMinor = (expenseMinor - topSum).coerceAtLeast(0L),
            upcomingDues = upcomingDues.map {
                HomeDueRow(
                    debtId = it.debtId,
                    contactName = it.contactName,
                    direction = it.direction,
                    amountMinor = it.dueMinor,
                    dueEpochDay = it.dueDate,
                    daysUntilDue = it.dueDate - today
                )
            },
            recentTransactions = recentTransactions.map {
                HomeTransactionRow(
                    id = it.id,
                    kind = it.kind,
                    title = it.note ?: it.categoryName,
                    categoryName = it.categoryName,
                    categoryIconKey = it.categoryIconKey,
                    amountMinor = it.amountMinor,
                    occurredAt = it.occurredAt
                )
            }
        )
    }

    private fun minorToInput(minor: Long): String =
        if (minor % 100L == 0L) {
            (minor / 100L).toString()
        } else {
            String.format(Locale.ROOT, "%d.%02d", minor / 100L, minor % 100L)
        }

    private companion object {
        const val MAX_BUDGET_INPUT_LENGTH = 18
    }
}
