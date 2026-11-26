package com.appcraftbyk.expense.art.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcraftbyk.expense.art.core.time.AppClock
import com.appcraftbyk.expense.art.data.prefs.PreferencesRepository
import com.appcraftbyk.expense.art.data.prefs.UserPreferences
import com.appcraftbyk.expense.art.ui.addentry.AddEntryMode
import com.appcraftbyk.expense.art.ui.addentry.AddEntrySheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * Activity-scoped state shared by the shell: the selected month, user preferences, and the
 * global Add Entry sheet. Screen-local state (chips, filters, tabs) stays in the screen ViewModel.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val appClock: AppClock
) : ViewModel() {

    private val selectedMonthOverride = MutableStateFlow<YearMonth?>(null)
    private val _addEntrySheet = MutableStateFlow(AddEntrySheetState())

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    /** Explicit selection wins over the persisted month, which wins over "this month". */
    val selectedYearMonth: StateFlow<YearMonth> = combine(
        selectedMonthOverride,
        userPreferences
    ) { selected, prefs ->
        selected ?: prefs.lastSelectedMonth ?: appClock.currentYearMonth()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        appClock.currentYearMonth()
    )

    val addEntrySheet: StateFlow<AddEntrySheetState> = _addEntrySheet.asStateFlow()

    fun selectYearMonth(yearMonth: YearMonth) {
        selectedMonthOverride.value = yearMonth
        viewModelScope.launch { preferencesRepository.setLastSelectedMonth(yearMonth) }
    }

    fun openAddEntry(mode: AddEntryMode) {
        _addEntrySheet.value = AddEntrySheetState(isOpen = true, presetMode = mode)
    }

    fun dismissAddEntry() {
        _addEntrySheet.value = _addEntrySheet.value.copy(isOpen = false)
    }
}
