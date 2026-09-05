package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
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
import com.lpstudio.bolaodagalera.presentation.bolao.AddParticipantsScreen
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
 * Testes de UI da tela de Adicionar Participantes via Robolectric. Usa o
 * bolão fake "bolao-1" (já pré-cadastrado no FakeBolaoRepository) e o
 * FAKE_USER como quem convida.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AddParticipantsScreenTest {
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
                AddParticipantsScreen(bolaoId = "bolao-1", onNavigateBack = {})
            }
        }
    }

    @Test
    fun convidar_usuario_inexistente_mostra_erro() {
        setContent()

        composeTestRule.onNodeWithText("E-mail, Telefone ou ID").performTextInput("ninguem@teste.com")
        composeTestRule.onNodeWithText("Enviar Convite").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        val expectedMessage = "Usuário não encontrado. Peça para seu amigo criar uma conta primeiro " +
            "ou compartilhe o link de convite abaixo."
        composeTestRule.onNodeWithText(expectedMessage, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun convidar_usuario_existente_por_email_mostra_sucesso() {
        setContent()

        // FAKE_FRIEND exists but isn't a participant of bolao-1 yet - unlike FAKE_USER
        // (the inviter) or the u3-u9 fakes, which are all already members.
        composeTestRule.onNodeWithText("E-mail, Telefone ou ID").performTextInput(FAKE_FRIEND.email)
        composeTestRule.onNodeWithText("Enviar Convite").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Convite enviado com sucesso!").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun convidar_usuario_que_ja_e_participante_mostra_erro() {
        setContent()

        composeTestRule.onNodeWithText("E-mail, Telefone ou ID").performTextInput(FAKE_USER.email)
        composeTestRule.onNodeWithText("Enviar Convite").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Esse usuário já participa deste bolão.")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
