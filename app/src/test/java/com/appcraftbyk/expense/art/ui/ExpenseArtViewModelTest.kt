package com.appcraftbyk.expense.art.ui

import androidx.lifecycle.SavedStateHandle
import com.appcraftbyk.expense.art.ui.navigation.AppTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseArtViewModelTest {

    @Test
    fun `starts on home with quick add hidden`() {
        val viewModel = ExpenseArtViewModel(SavedStateHandle())

        assertEquals(AppTab.Home, viewModel.uiState.value.selectedTab)
        assertFalse(viewModel.uiState.value.isQuickAddMenuVisible)
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
    fun `quick add menu opens and dismisses`() {
        val viewModel = ExpenseArtViewModel(SavedStateHandle())

        viewModel.onAddClick()
        assertTrue(viewModel.uiState.value.isQuickAddMenuVisible)

        viewModel.onQuickAddDismissed()
        assertFalse(viewModel.uiState.value.isQuickAddMenuVisible)
    }

    @Test
    fun `restores selected tab from saved state`() {
        val viewModel = ExpenseArtViewModel(
            SavedStateHandle(mapOf("selected_tab" to AppTab.Expenses.name))
        )

        assertEquals(AppTab.Expenses, viewModel.uiState.value.selectedTab)
    }
}
