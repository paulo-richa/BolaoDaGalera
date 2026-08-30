package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.auth.RegisterScreen
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

/**
 * Testes de UI da tela de Cadastro via Robolectric. Segue o mesmo padrão e
 * as mesmas correções descobertas em LoginScreenTest:
 * - performScrollTo() antes de assertIsDisplayed()/performClick() (a janela
 *   simulada é menor que o conteúdo da Column com scroll).
 * - Dispatchers.setMain(UnconfinedTestDispatcher()) para as coroutines do
 *   ViewModel (checkEmail, register, generateAvailableUsername) rodarem.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RegisterScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()
        authRepository = FakeAuthRepository()
        authRepository.setUser(null)

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

    private fun setRegisterContent(initialEmail: String = "", onRegisterSuccess: () -> Unit = {}, onNavigateBack: () -> Unit = {}) {
        composeTestRule.setContent {
            AppTheme {
                RegisterScreen(
                    initialEmail = initialEmail,
                    onRegisterSuccess = onRegisterSuccess,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }

    @Test
    fun tela_inicial_pre_preenche_email_e_mantem_botao_desabilitado() {
        setRegisterContent(initialEmail = "novo@teste.com")

        composeTestRule.onAllNodesWithText("Criar conta")
            .filterToOne(hasClickAction())
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun nome_incompleto_mostra_erro_de_validacao() {
        setRegisterContent()

        composeTestRule.onNodeWithText("Nome Completo").performTextInput("Fulano")

        composeTestRule.onNodeWithText("Digite seu nome e sobrenome").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun senhas_diferentes_mostram_erro_de_confirmacao() {
        setRegisterContent()

        composeTestRule.onNodeWithText("Nome Completo").performTextInput("Fulano de Tal")
        composeTestRule.onNodeWithText("E-mail (ex: joaosilva@gmail.com)").performTextInput("fulano@teste.com")
        composeTestRule.onNodeWithText("Senha (min. 6 caracteres)").performTextInput("123456")
        composeTestRule.onNodeWithText("Confirmar senha").performTextInput("654321")

        composeTestRule.onNodeWithText("As senhas não coincidem").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Criar conta")
            .filterToOne(hasClickAction())
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun formulario_valido_habilita_botao_criar_conta() {
        setRegisterContent()

        composeTestRule.onNodeWithText("Nome Completo").performTextInput("Fulano de Tal")
        composeTestRule.onNodeWithText("E-mail (ex: joaosilva@gmail.com)").performTextInput("fulano@teste.com")
        composeTestRule.onNodeWithText("Senha (min. 6 caracteres)").performTextInput("123456")
        composeTestRule.onNodeWithText("Confirmar senha").performTextInput("123456")

        composeTestRule.onAllNodesWithText("Criar conta")
            .filterToOne(hasClickAction())
            .performScrollTo()
            .assertIsEnabled()
    }

    @Test
    fun email_ja_cadastrado_mostra_erro_ao_tentar_criar_conta() {
        setRegisterContent()

        composeTestRule.onNodeWithText("Nome Completo").performTextInput("Fulano de Tal")
        composeTestRule.onNodeWithText("E-mail (ex: joaosilva@gmail.com)").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Senha (min. 6 caracteres)").performTextInput("123456")
        composeTestRule.onNodeWithText("Confirmar senha").performTextInput("123456")

        composeTestRule.onAllNodesWithText("Criar conta").filterToOne(hasClickAction()).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Este e-mail já está em uso.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cadastro_com_sucesso_chama_onRegisterSuccess() {
        var successCount = 0
        setRegisterContent(onRegisterSuccess = { successCount++ })

        composeTestRule.onNodeWithText("Nome Completo").performTextInput("Fulano Novato")
        composeTestRule.onNodeWithText("E-mail (ex: joaosilva@gmail.com)").performTextInput("fulanonovato@teste.com")
        composeTestRule.onNodeWithText("Senha (min. 6 caracteres)").performTextInput("123456")
        composeTestRule.onNodeWithText("Confirmar senha").performTextInput("123456")

        composeTestRule.onAllNodesWithText("Criar conta").filterToOne(hasClickAction()).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assert(successCount == 1) { "onRegisterSuccess deveria ter sido chamado uma vez, foi chamado $successCount" }
    }
}
