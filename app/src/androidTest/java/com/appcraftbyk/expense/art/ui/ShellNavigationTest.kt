package com.appcraftbyk.expense.art.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appcraftbyk.expense.art.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShellNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tabsSwitch_fabFollowsTab_andSettingsSurviveRecreation() {
        // Tab switch reaches the Expenses placeholder.
        composeRule.onNodeWithText("Expenses").performClick()
        composeRule.onNodeWithText("The expense and income ledger arrives in a later phase.")
            .assertIsDisplayed()

        // Reminders is a placeholder tab and intentionally has no FAB (D8).
        composeRule.onNodeWithText("Reminders").performClick()
        composeRule.onNodeWithContentDescription("Add entry").assertDoesNotExist()

        composeRule.onNodeWithText("Home").performClick()
        composeRule.onNodeWithContentDescription("Add entry").assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Overview").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.onNodeWithText("Dark").performClick()
        val name = "Asha ${System.currentTimeMillis()}"
        composeRule.onNode(hasSetTextAction()).performTextReplacement(name)
        composeRule.onNodeWithText("Save").performClick()

        // The snackbar is emitted after both DataStore writes complete.
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Profile name saved").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.activityRule.scenario.recreate()

        // Activity recreation is a restart of the app's UI; settings must come back from DataStore.
        if (composeRule.onAllNodesWithContentDescription("Settings").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithContentDescription("Settings").performClick()
        }
        composeRule.onNodeWithText("Dark").assertIsSelected()
        composeRule.onNode(hasSetTextAction()).assertTextContains(name)
    }
}
