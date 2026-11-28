package com.appcraftbyk.expense.art.ui

import androidx.lifecycle.SavedStateHandle
import com.appcraftbyk.expense.art.ui.navigation.AppTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseArtViewModelTest {

    @Test
    fun `starts on home with no sheet`() {
        val viewModel = ExpenseArtViewModel(SavedStateHandle())

        assertEquals(AppTab.Home, viewModel.uiState.value.selectedTab)
        assertEquals(AppSheet.None, viewModel.uiState.value.sheet)
    }

    @Test
    fun `selecting a tab updates state and saved state`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = ExpenseArtViewModel(savedStateHandle)

        viewModel.onTabSelected(AppTab.Reminders)

        assertEquals(AppTab.Reminders, viewModel.uiState.value.selectedTab)
        assertEquals(AppTab.Reminders.name, savedStateHandle.get<String>("selected_tab"))
    }

    @Test
    fun `add click opens the entry sheet and dismiss closes it`() {
        val viewModel = ExpenseArtViewModel(SavedStateHandle())

        viewModel.onAddClick()
        assertEquals(AppSheet.AddEntry, viewModel.uiState.value.sheet)

        viewModel.onSheetDismissed()
        assertEquals(AppSheet.None, viewModel.uiState.value.sheet)
    }

    @Test
    fun `restores selected tab from saved state`() {
        val viewModel = ExpenseArtViewModel(
            SavedStateHandle(mapOf("selected_tab" to AppTab.Expenses.name))
        )

        assertEquals(AppTab.Expenses, viewModel.uiState.value.selectedTab)
    }
}
