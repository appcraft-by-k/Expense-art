package com.appcraftbyk.expense.art.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.appcraftbyk.expense.art.ExpenseArtApplication
import com.appcraftbyk.expense.art.data.CategoryRepository
import com.appcraftbyk.expense.art.data.PaymentMethodRepository
import com.appcraftbyk.expense.art.data.TransactionRepository
import com.appcraftbyk.expense.art.data.local.CategoryEntity
import com.appcraftbyk.expense.art.data.local.PaymentMethodEntity
import com.appcraftbyk.expense.art.data.local.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** Immutable state for the add-expense / add-income form. */
data class AddEntryUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val amountMinor: Long = 0,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isCategoriesLoaded: Boolean = false,
    val paymentMethods: List<PaymentMethodEntity> = emptyList(),
    val selectedPaymentMethodId: Long? = null,
    val isPaymentMethodsLoaded: Boolean = false,
    val occurredAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSaving: Boolean = false,
    val amountError: Boolean = false,
    val saveError: Boolean = false
) {
    val canSave: Boolean
        get() = amountMinor > 0 && selectedCategoryId != null && !isSaving
}

/** Creates income and expense entries through [TransactionRepository]. */
@OptIn(ExperimentalCoroutinesApi::class)
class AddEntryViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState(occurredAt = now()))
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    private val _savedEntryId = MutableStateFlow<Long?>(null)
    val savedEntryId: StateFlow<Long?> = _savedEntryId.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.map { it.type }
                .distinctUntilChanged()
                .flatMapLatest { categoryRepository.observeByType(it) }
                .collect { categories ->
                    _uiState.update { state ->
                        state.copy(
                            categories = categories,
                            selectedCategoryId = state.selectedCategoryId
                                ?.takeIf { id -> categories.any { it.id == id } }
                                ?: categories.firstOrNull()?.id,
                            isCategoriesLoaded = true
                        )
                    }
                }
        }

        viewModelScope.launch {
            paymentMethodRepository.observeAll().collect { paymentMethods ->
                _uiState.update { state ->
                    state.copy(
                        paymentMethods = paymentMethods,
                        selectedPaymentMethodId = state.selectedPaymentMethodId
                            ?.takeIf { id -> paymentMethods.any { it.id == id } }
                            ?: paymentMethods.firstOrNull()?.id,
                        isPaymentMethodsLoaded = true
                    )
                }
            }
        }
    }

    /** Resets the form and prepares it for [type]. */
    fun start(type: TransactionType) {
        _savedEntryId.value = null
        _uiState.update { current ->
            AddEntryUiState(
                type = type,
                categories = current.categories,
                selectedCategoryId = current.categories.firstOrNull { it.type == type }?.id,
                isCategoriesLoaded = current.isCategoriesLoaded,
                paymentMethods = current.paymentMethods,
                selectedPaymentMethodId = current.selectedPaymentMethodId
                    ?.takeIf { id -> current.paymentMethods.any { it.id == id } }
                    ?: current.paymentMethods.firstOrNull()?.id,
                isPaymentMethodsLoaded = current.isPaymentMethodsLoaded,
                occurredAt = now()
            )
        }
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.update { state ->
            if (state.type == type) {
                state
            } else {
                state.copy(
                    type = type,
                    selectedCategoryId = null,
                    amountError = false,
                    saveError = false
                )
            }
        }
    }

    fun onAmountChanged(raw: String) {
        val input = sanitizeAmountInput(raw)
        _uiState.update {
            it.copy(
                amountInput = input,
                amountMinor = parseAmountMinor(input),
                amountError = false,
                saveError = false
            )
        }
    }

    fun onQuickAmountAdded(amountMinor: Long) {
        _uiState.update { state ->
            val updated = (state.amountMinor + amountMinor).coerceAtMost(MAX_AMOUNT_MINOR)
            state.copy(
                amountInput = formatAmountInput(updated),
                amountMinor = updated,
                amountError = false,
                saveError = false
            )
        }
    }

    fun onClearAmount() {
        _uiState.update {
            it.copy(amountInput = "", amountMinor = 0, amountError = false, saveError = false)
        }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, saveError = false) }
    }

    fun onPaymentMethodSelected(paymentMethodId: Long?) {
        _uiState.update { it.copy(selectedPaymentMethodId = paymentMethodId, saveError = false) }
    }

    fun onDateTimeSelected(occurredAt: Long) {
        _uiState.update { it.copy(occurredAt = occurredAt) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        val state = _uiState.value
        if (state.amountMinor <= 0) {
            _uiState.update { it.copy(amountError = true) }
            return
        }

        val categoryId = state.selectedCategoryId
        if (categoryId == null) {
            _uiState.update { it.copy(saveError = true) }
            return
        }

        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, saveError = false) }
        viewModelScope.launch {
            try {
                val id = when (state.type) {
                    TransactionType.EXPENSE -> transactionRepository.addExpense(
                        amountMinor = state.amountMinor,
                        categoryId = categoryId,
                        occurredAt = state.occurredAt,
                        note = state.note,
                        paymentMethodId = state.selectedPaymentMethodId
                    )

                    TransactionType.INCOME -> transactionRepository.addIncome(
                        amountMinor = state.amountMinor,
                        categoryId = categoryId,
                        occurredAt = state.occurredAt,
                        note = state.note,
                        paymentMethodId = state.selectedPaymentMethodId
                    )
                }

                _uiState.update { current ->
                    AddEntryUiState(
                        type = state.type,
                        categories = current.categories,
                        isCategoriesLoaded = current.isCategoriesLoaded,
                        paymentMethods = current.paymentMethods,
                        isPaymentMethodsLoaded = current.isPaymentMethodsLoaded,
                        occurredAt = now()
                    )
                }
                _savedEntryId.value = id
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = true) }
            }
        }
    }

    fun consumeSaved() {
        _savedEntryId.value = null
    }

    companion object {
        fun factory(application: ExpenseArtApplication): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    AddEntryViewModel(
                        transactionRepository = application.transactionRepository,
                        categoryRepository = application.categoryRepository,
                        paymentMethodRepository = application.paymentMethodRepository
                    )
                }
            }
    }
}

private const val MAX_AMOUNT_DIGITS = 9
private const val MAX_AMOUNT_MINOR = 999_999_999L * 100

/** Keeps only digits and one decimal separator, with at most two decimal places. */
internal fun sanitizeAmountInput(raw: String): String {
    val builder = StringBuilder()
    var hasDecimal = false
    var digits = 0

    for (char in raw) {
        when {
            char.isDigit() && digits < MAX_AMOUNT_DIGITS -> {
                builder.append(char)
                digits++
            }

            char == '.' && !hasDecimal -> {
                builder.append(char)
                hasDecimal = true
            }
        }
    }

    val value = builder.toString()
    val parts = value.split('.')
    return if (parts.size == 2) "${parts[0]}.${parts[1].take(2)}" else value
}

/** Parses a major-unit amount such as "1450.50" into minor units. */
internal fun parseAmountMinor(input: String): Long {
    if (input.isBlank()) return 0

    val parts = input.split('.')
    val rupees = parts.getOrNull(0)?.toLongOrNull() ?: return 0
    val paise = parts.getOrNull(1)?.padEnd(2, '0')?.take(2)?.toLongOrNull() ?: 0
    return rupees * 100 + paise
}

/** Formats minor units for the amount field without grouping separators. */
internal fun formatAmountInput(amountMinor: Long): String {
    val rupees = amountMinor / 100
    val paise = amountMinor % 100
    return if (paise == 0L) {
        rupees.toString()
    } else {
        String.format(Locale.US, "%d.%02d", rupees, paise)
    }
}
