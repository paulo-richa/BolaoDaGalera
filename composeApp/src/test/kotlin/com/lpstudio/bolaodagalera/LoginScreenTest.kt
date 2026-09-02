package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.auth.LoginScreen
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
 * Testes de UI da tela de Login via Robolectric (JVM, sem emulador).
 *
 * Tentei rodar como teste instrumentado real (androidTest, no emulador), mas
 * o emulador disponível está na API 37 e o AndroidX Test/Espresso — mesmo na
 * versão mais recente disponível (3.7.0) — ainda não suporta essa API
 * (NoSuchMethodException em InputManager.getInstance, bug ainda não corrigido
 * upstream). Robolectric roda a mesma árvore de Compose UI Testing sem
 * depender do emulador, seguindo o padrão já usado em SnapshotTest.kt.
 *
 * A tela inteira fica dentro de uma Column com scroll vertical, e a "janela"
 * simulada pelo Robolectric é mais baixa que o conteúdo — por isso, sempre
 * que o nó pode estar "abaixo da dobra", chamamos performScrollTo() antes de
 * assertIsDisplayed() (assertIsEnabled/NotEnabled não sofrem disso, pois não
 * dependem da posição visual do nó).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoginScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var authRepository: FakeAuthRepository

    @Before
    fun setup() {
        // Without this, the coroutine launched in viewModelScope.launch (checkEmail,
        // login, etc.) never runs: Dispatchers.Main does not exist by default in
        // JVM/Robolectric tests. UnconfinedTestDispatcher executes the coroutine
        // immediately and synchronously when launched.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()
        authRepository = FakeAuthRepository()
        authRepository.setUser(null) // Starts logged out, on the Login screen

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

    private fun setLoginContent(onLoginSuccess: () -> Unit = {}, onNavigateToRegister: (String?) -> Unit = {}) {
        composeTestRule.setContent {
            AppTheme {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }
    }

    @Test
    fun tela_inicial_mostra_campo_de_email_e_botao_continuar_desabilitado() {
        setLoginContent()

        composeTestRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").assertIsNotEnabled()
    }

    @Test
    fun email_invalido_mostra_erro_e_mantem_botao_desabilitado() {
        setLoginContent()

        composeTestRule.onNodeWithText("E-mail").performTextInput("email-invalido")

        composeTestRule.onNodeWithText("E-mail inválido").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar").assertIsNotEnabled()
    }

    @Test
    fun email_valido_habilita_botao_continuar() {
        setLoginContent()

        composeTestRule.onNodeWithText("E-mail").performTextInput(FAKE_USER.email)

        composeTestRule.onNodeWithText("Continuar").assertIsEnabled()
    }

    @Test
    fun email_sem_cadastro_oferece_criar_conta() {
        var identifierPassedToRegister: String? = "not-called"
        setLoginContent(onNavigateToRegister = { identifierPassedToRegister = it })

        composeTestRule.onNodeWithText("E-mail").performTextInput("novo-usuario@teste.com")
        composeTestRule.onNodeWithText("Continuar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Este e-mail ainda não possui conta no Bolão da Galera.")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("CRIAR CONTA AGORA").performScrollTo().performClick()
        assert(identifierPassedToRegister == "novo-usuario@teste.com") {
            "Esperava que onNavigateToRegister recebesse o e-mail digitado, veio: $identifierPassedToRegister"
        }
    }

    @Test
    fun email_cadastrado_mostra_campo_de_senha() {
        setLoginContent()

        composeTestRule.onNodeWithText("E-mail").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Continuar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Senha").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Entrar").assertIsNotEnabled()
    }

    @Test
    fun esqueceu_a_senha_envia_email_de_recuperacao() {
        setLoginContent()

        composeTestRule.onNodeWithText("E-mail").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Continuar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Esqueceu a senha?").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("E-mail de recuperação enviado com sucesso!")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun senha_errada_mostra_mensagem_de_erro_e_nao_avanca() {
        var loginSuccessCount = 0
        setLoginContent(onLoginSuccess = { loginSuccessCount++ })

        composeTestRule.onNodeWithText("E-mail").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Continuar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Senha").performTextInput("senha-errada")
        composeTestRule.onNodeWithText("Entrar").performScrollTo().performClick()

        composeTestRule.onNodeWithText("E-mail ou senha incorretos. Verifique os dados e tente novamente.")
            .performScrollTo()
            .assertIsDisplayed()
        assert(loginSuccessCount == 0) { "onLoginSuccess não deveria ter sido chamado com senha errada" }
    }

    @Test
    fun login_com_credenciais_corretas_chama_onLoginSuccess() {
        var loginSuccessCount = 0
        setLoginContent(onLoginSuccess = { loginSuccessCount++ })

        composeTestRule.onNodeWithText("E-mail").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Continuar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Senha").performTextInput(authRepository.validPassword)
        composeTestRule.onNodeWithText("Entrar").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assert(loginSuccessCount == 1) {
            "onLoginSuccess deveria ter sido chamado uma vez, foi chamado $loginSuccessCount"
        }
    }
}
