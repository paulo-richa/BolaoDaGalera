package com.lpstudio.bolaodagalera.presentation.home

import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeInvitationRepository
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.model.User
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var invitationRepository: FakeInvitationRepository
    private lateinit var predictionRepository: FakePredictionRepository
    private lateinit var viewModel: HomeViewModel

    private val testUser = User(id = "user-1", name = "Test User", email = "test@test.com", username = "testuser")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authRepository = FakeAuthRepository()
        authRepository.setUser(testUser)

        matchRepository = FakeMatchRepository()
        predictionRepository = FakePredictionRepository(matchRepository)
        bolaoRepository = FakeBolaoRepository()
        invitationRepository = FakeInvitationRepository()

        viewModel =
            HomeViewModel(
                authRepository = authRepository,
                bolaoRepository = bolaoRepository,
                matchRepository = matchRepository,
                invitationRepository = invitationRepository,
                predictionRepository = predictionRepository
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have user and load boloes`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(testUser, state.user)
        assertFalse(state.isLoading)
        // O FakeBolaoRepository pode vir vazio ou com dados iniciais dependendo da implementação
        assertNotNull(state.boloes)
    }

    @Test
    fun `signing out should clear user and boloes`() = runTest {
        viewModel.signOut()
        val state = viewModel.uiState.value
        assertEquals(null, state.user)
        assertEquals(emptyList(), state.boloes)
    }

    // ---------- CONVITES ----------

    @Test
    fun `aceitar convite adiciona o usuario ao bolao e remove o convite da lista`() = runTest {
        invitationRepository.sendInvitation(
            bolaoId = "bolao-1",
            bolaoName = "Bolão da Galera",
            inviterName = "Dono do Bolão",
            inviteeIdentifier = testUser.id
        )
        // Aguarda o combine reprocessar após o novo convite
        val invitation = viewModel.uiState.value.invitations.find { it.bolaoId == "bolao-1" }
        assertNotNull(invitation)

        viewModel.respondToInvitation(invitation.id, accept = true)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertTrue(testUser.id in bolao.participants)
        assertTrue(viewModel.uiState.value.invitations.none { it.bolaoId == "bolao-1" })
    }

    @Test
    fun `recusar convite nao adiciona o usuario ao bolao mas remove o convite`() = runTest {
        invitationRepository.sendInvitation(
            bolaoId = "bolao-1",
            bolaoName = "Bolão da Galera",
            inviterName = "Dono do Bolão",
            inviteeIdentifier = testUser.id
        )
        val invitation = viewModel.uiState.value.invitations.find { it.bolaoId == "bolao-1" }
        assertNotNull(invitation)

        viewModel.respondToInvitation(invitation.id, accept = false)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse(testUser.id in bolao.participants)
        assertTrue(viewModel.uiState.value.invitations.none { it.bolaoId == "bolao-1" })
    }

    @Test
    fun `responder convite inexistente nao faz nada`() = runTest {
        viewModel.respondToInvitation("convite-que-nao-existe", accept = true)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse(testUser.id in bolao.participants)
    }
}
