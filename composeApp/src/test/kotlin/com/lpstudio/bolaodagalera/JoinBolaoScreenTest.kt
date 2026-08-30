package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lpstudio.bolaodagalera.data.fake.FAKE_FRIEND
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.bolao.JoinBolaoScreen
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
 * Testes de UI da tela de Entrar em Bolão via Robolectric. O bolão fake
 * "bolao-1" tem código "LIB026" e já inclui FAKE_USER como participante.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class JoinBolaoScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun startKoinWithUser(user: com.lpstudio.bolaodagalera.domain.model.User) {
        val authRepository = FakeAuthRepository()
        authRepository.setUser(user)
        startKoin {
            modules(
                fakeAppModule,
                module { single<AuthRepository> { authRepository } }
            )
        }
    }

    private fun setJoinBolaoContent(onJoined: (String) -> Unit = {}, onNavigateBack: () -> Unit = {}) {
        composeTestRule.setContent {
            AppTheme {
                JoinBolaoScreen(onJoined = onJoined, onNavigateBack = onNavigateBack)
            }
        }
    }

    @Test
    fun codigo_incompleto_mantem_botao_desabilitado() {
        startKoinWithUser(FAKE_FRIEND)
        setJoinBolaoContent()

        composeTestRule.onNodeWithText("Código").performTextInput("LIB2")

        composeTestRule.onNodeWithText("Entrar no Bolão").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun codigo_completo_habilita_botao() {
        startKoinWithUser(FAKE_FRIEND)
        setJoinBolaoContent()

        composeTestRule.onNodeWithText("Código").performTextInput("LIB026")

        composeTestRule.onNodeWithText("Entrar no Bolão").performScrollTo().assertIsEnabled()
    }

    @Test
    fun codigo_invalido_mostra_erro() {
        startKoinWithUser(FAKE_FRIEND)
        setJoinBolaoContent()

        composeTestRule.onNodeWithText("Código").performTextInput("ZZZZZZ")
        composeTestRule.onNodeWithText("Entrar no Bolão").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Bolão não encontrado com o código ZZZZZZ").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun novo_participante_ve_dialogo_de_solicitacao_enviada() {
        startKoinWithUser(FAKE_FRIEND)
        setJoinBolaoContent()

        composeTestRule.onNodeWithText("Código").performTextInput("LIB026")
        composeTestRule.onNodeWithText("Entrar no Bolão").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Solicitação Enviada!").assertIsDisplayed()
    }

    @Test
    fun membro_existente_navega_direto_sem_dialogo() {
        var joinedBolaoId: String? = null
        startKoinWithUser(FAKE_USER) // já é participante do bolao-1
        setJoinBolaoContent(onJoined = { joinedBolaoId = it })

        composeTestRule.onNodeWithText("Código").performTextInput("LIB026")
        composeTestRule.onNodeWithText("Entrar no Bolão").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assert(joinedBolaoId == "bolao-1") { "onJoined deveria ter sido chamado com bolao-1, veio: $joinedBolaoId" }
    }
}
