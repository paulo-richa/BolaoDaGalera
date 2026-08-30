package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.bolao.CreateBolaoScreen
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
 * Testes de UI da tela de Criação de Bolão via Robolectric. Segue o mesmo
 * padrão descoberto em LoginScreenTest/RegisterScreenTest.
 *
 * Usa um campeonato "Pontos Corridos" fake (isPointsBased = true) para evitar
 * a complexidade das opções de fase/escopo (grupos, mata-mata), que dependem
 * de datas de jogos reais e não são o foco deste teste.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateBolaoScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeChampionship =
        Championship(
            id = "BRASILEIRAO",
            displayName = "Brasileirão",
            emoji = "🏆",
            apiCode = "BSA",
            hasStandings = true,
            isPointsBased = true,
            isGroupsAndKnockout = false,
            isAvailable = true
        )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()
        Championship.setCache(listOf(fakeChampionship))

        startKoin {
            modules(
                fakeAppModule,
                module { single<AuthRepository> { FakeAuthRepository() } }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun setCreateBolaoContent(onCreated: (String) -> Unit = {}) {
        composeTestRule.setContent {
            AppTheme {
                CreateBolaoScreen(
                    onCreated = onCreated,
                    onNavigateToAddParticipants = {},
                    onNavigateBack = {}
                )
            }
        }
    }

    @Test
    fun nome_curto_mantem_botao_criar_desabilitado() {
        setCreateBolaoContent()

        composeTestRule.onNodeWithText("Nome do bolão *").performTextInput("Curto")

        composeTestRule.onNodeWithText("Criar Bolão").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun nome_valido_habilita_botao_criar() {
        setCreateBolaoContent()

        composeTestRule.onNodeWithText("Nome do bolão *").performTextInput("Bolão dos Amigos")

        composeTestRule.onNodeWithText("Criar Bolão").performScrollTo().assertIsEnabled()
    }

    @Test
    fun criar_bolao_com_sucesso_mostra_dialogo_com_codigo() {
        setCreateBolaoContent()

        composeTestRule.onNodeWithText("Nome do bolão *").performTextInput("Bolão dos Amigos")
        composeTestRule.onNodeWithText("Criar Bolão").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Bolão Criado!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seu código de convite é:").assertIsDisplayed()
    }

    @Test
    fun ir_para_o_bolao_apos_criar_chama_onCreated() {
        var createdBolaoId: String? = null
        setCreateBolaoContent(onCreated = { createdBolaoId = it })

        composeTestRule.onNodeWithText("Nome do bolão *").performTextInput("Bolão dos Amigos")
        composeTestRule.onNodeWithText("Criar Bolão").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Ir para o bolão").performClick()

        assert(createdBolaoId != null) { "onCreated deveria ter sido chamado com o id do bolão criado" }
    }
}
