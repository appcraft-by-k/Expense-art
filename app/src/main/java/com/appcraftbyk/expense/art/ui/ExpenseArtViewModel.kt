package com.appcraftbyk.expense.art.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.appcraftbyk.expense.art.ui.navigation.AppTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Which sheet, if any, is currently presented over the app shell. */
sealed interface AppSheet {
    data object None : AppSheet
    data object AddEntry : AppSheet
}

/** Immutable state rendered by the app shell. */
data class ExpenseArtUiState(
    val selectedTab: AppTab = AppTab.Home,
    val sheet: AppSheet = AppSheet.None
)

/** Owns the app shell state so it survives configuration changes. */
class ExpenseArtViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExpenseArtUiState(
            selectedTab = savedStateHandle.get<String>(KEY_SELECTED_TAB).toAppTab()
        )
    )
    val uiState: StateFlow<ExpenseArtUiState> = _uiState.asStateFlow()

    fun onTabSelected(tab: AppTab) {
        savedStateHandle[KEY_SELECTED_TAB] = tab.name
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onAddClick() {
        _uiState.update { it.copy(sheet = AppSheet.AddEntry) }
    }

    fun onSheetDismissed() {
        _uiState.update { it.copy(sheet = AppSheet.None) }
    }

    private fun String?.toAppTab(): AppTab =
        this?.let { name -> AppTab.entries.firstOrNull { it.name == name } } ?: AppTab.Home

    private companion object {
        const val KEY_SELECTED_TAB = "selected_tab"
    }
}
