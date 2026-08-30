package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.auth.ProfileScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()

        val authRepository = FakeAuthRepository()
        authRepository.setUser(FAKE_USER)

        startKoin {
            modules(
                fakeAppModule,
                module { single<AuthRepository> { authRepository } }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AppTheme {
                ProfileScreen(onNavigateToHelp = {}, onNavigateBack = {}, onSignOut = {})
            }
        }
    }

    @Test
    fun tela_pre_preenche_dados_do_usuario_atual() {
        setContent()

        composeTestRule.onNodeWithText(FAKE_USER.email).assertIsDisplayed()
        composeTestRule.onNodeWithText(FAKE_USER.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(FAKE_USER.nickname).assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar Alterações").performScrollTo().assertIsEnabled()
    }

    @Test
    fun nome_incompleto_desabilita_salvar_e_mostra_erro() {
        setContent()

        composeTestRule.onNodeWithText("Nome Completo (ex: João da Silva)").performTextClearance()
        composeTestRule.onNodeWithText("Nome Completo (ex: João da Silva)").performTextInput("Fulano")

        composeTestRule.onNodeWithText("Digite seu nome e sobrenome").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar Alterações").performScrollTo().assertIsNotEnabled()
    }
}
