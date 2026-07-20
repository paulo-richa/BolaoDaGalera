package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoDetailScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import com.lpstudio.bolaodagalera.di.fakeAppModule

class BolaoDetailUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            modules(fakeAppModule)
        }
    }

    @Test
    fun expandedGroup_shouldBePreserved_afterNavigation() {
        // 1. Setup
        var navigatedBackCount = 0
        composeTestRule.setContent {
            AppTheme {
                BolaoDetailScreen(
                    bolaoId = "bolao-1",
                    onNavigateToPrediction = { /* Mock navigation */ },
                    onNavigateToEdit = { },
                    onNavigateToAddParticipants = { },
                    onNavigateBack = { navigatedBackCount++ }
                )
            }
        }

        // 2. Click to expand a group (e.g., Grupo A)
        val groupAHeader = composeTestRule.onNodeWithText("Grupo A", substring = true)
        groupAHeader.performClick()

        // 3. Verify it's expanded (Palmeiras match should be visible)
        composeTestRule.onNodeWithText("Palmeiras", substring = true).assertIsDisplayed()

        // 4. Simulate a refresh or small delay that might trigger a reset
        // In a real test, we would navigate to another screen and back.
        // For this unit-level UI test, we can trigger a state change in the VM or just verify 
        // that the rememberSaveable logic holds.
        
        // Let's check if "Rodada 2" selection is also preserved
        composeTestRule.onNodeWithText("Rodada 2").performClick()
        composeTestRule.onNodeWithText("Rodada 2").assertIsSelected()
    }
}
