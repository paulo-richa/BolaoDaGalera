package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoDetailScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SnapshotTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            modules(fakeAppModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun captureBolaoDetail() {
        composeTestRule.setContent {
            AppTheme {
                BolaoDetailScreen(
                    bolaoId = "bolao-1",
                    onNavigateToPrediction = {},
                    onNavigateToAllPredictions = {},
                    onNavigateToEdit = {},
                    onNavigateToAddParticipants = {},
                    onNavigateToHelp = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage()
    }
}
