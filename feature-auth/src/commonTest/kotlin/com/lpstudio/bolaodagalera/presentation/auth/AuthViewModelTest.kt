package com.lpstudio.bolaodagalera.presentation.auth

import com.lpstudio.bolaodagalera.data.fake.FAKE_FRIEND
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAnalyticsTracker
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        // Start signed out: login/registration tests simulate a fresh app install.
        authRepository.setUser(null)
        viewModel = AuthViewModel(authRepository, FakeCrashReporter(), FakePerformanceMonitor(), FakeAnalyticsTracker())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- LOGIN ----------

    @Test
    fun `login com credenciais corretas autentica o usuario`() = runTest {
        viewModel.login(FAKE_USER.email, authRepository.validPassword)

        val state = viewModel.uiState.value
        assertEquals(FAKE_USER.id, state.user?.id)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `login normaliza espacos em branco no email`() = runTest {
        viewModel.login("  ${FAKE_USER.email}  ", authRepository.validPassword)

        assertEquals(FAKE_USER.id, viewModel.uiState.value.user?.id)
    }

    @Test
    fun `login com senha errada mostra erro amigavel e nao autentica`() = runTest {
        viewModel.login(FAKE_USER.email, "senha-errada")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertFalse(state.isLoading)
        assertEquals("E-mail ou senha incorretos. Verifique os dados e tente novamente.", state.error)
    }

    @Test
    fun `login com email nao cadastrado mostra erro amigavel`() = runTest {
        viewModel.login("nao-existe@teste.com", authRepository.validPassword)

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Usuário não encontrado. Crie uma conta para acessar.", state.error)
    }

    @Test
    fun `login com erro de rede mostra mensagem de conexao`() = runTest {
        authRepository.signInException = Exception("network error: timeout")

        viewModel.login(FAKE_USER.email, authRepository.validPassword)

        assertEquals("Erro de conexão. Verifique sua internet e tente novamente.", viewModel.uiState.value.error)
    }

    @Test
    fun `login com muitas tentativas mostra mensagem de bloqueio`() = runTest {
        authRepository.signInException = Exception("too many requests")

        viewModel.login(FAKE_USER.email, authRepository.validPassword)

        assertEquals(
            "Muitas tentativas falhas. Sua conta foi temporariamente bloqueada por segurança.",
            viewModel.uiState.value.error
        )
    }

    // ---------- CHECK EMAIL (login step 1) ----------

    @Test
    fun `checkEmail em branco mostra erro sem chamar o repositorio`() = runTest {
        viewModel.checkEmail("")

        val state = viewModel.uiState.value
        assertEquals("Digite seu e-mail para continuar.", state.error)
        assertNull(state.emailExists)
    }

    @Test
    fun `checkEmail com email existente marca emailExists true`() = runTest {
        viewModel.checkEmail(FAKE_USER.email)

        val state = viewModel.uiState.value
        assertEquals(true, state.emailExists)
        assertEquals(FAKE_USER.email, state.checkedEmail)
        assertFalse(state.isLoading)
    }

    @Test
    fun `checkEmail com email inexistente marca emailExists false`() = runTest {
        viewModel.checkEmail("novo@teste.com")

        assertEquals(false, viewModel.uiState.value.emailExists)
    }

    @Test
    fun `resetEmailCheck limpa emailExists e checkedEmail`() = runTest {
        viewModel.checkEmail(FAKE_USER.email)
        assertNotNull(viewModel.uiState.value.emailExists)

        viewModel.resetEmailCheck()

        val state = viewModel.uiState.value
        assertNull(state.emailExists)
        assertTrue(state.checkedEmail.isEmpty())
    }

    @Test
    fun `clearError remove mensagens de erro e sucesso`() = runTest {
        viewModel.login(FAKE_USER.email, "senha-errada")
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertNull(state.successMessage)
    }

    // ---------- REGISTER ----------

    @Test
    fun `register com email ja em uso mostra erro e nao cria conta`() = runTest {
        viewModel.register(FAKE_USER.email, "123456", "Novo Usuario", "", "", "novousuario")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Este e-mail já está em uso.", state.error)
    }

    @Test
    fun `register com username ja em uso mostra erro e nao cria conta`() = runTest {
        viewModel.register("novo@teste.com", "123456", "Novo Usuario", "", "", FAKE_USER.username)

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Este ID já está em uso.", state.error)
    }

    @Test
    fun `register com telefone ja em uso mostra erro e nao cria conta`() = runTest {
        viewModel.register("novo@teste.com", "123456", "Novo Usuario", FAKE_USER.phone, "", "novousuario")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Este telefone já está em uso.", state.error)
    }

    @Test
    fun `register com apelido ja em uso mostra erro e nao cria conta`() = runTest {
        viewModel.register("novo@teste.com", "123456", "Novo Usuario", "", FAKE_USER.nickname, "novousuario")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Este apelido já está em uso.", state.error)
    }

    @Test
    fun `register com dados novos cria a conta com sucesso`() = runTest {
        viewModel.register("novo@teste.com", "123456", "Fulano Novo", "11900000000", "Fulaninho", "fulanonovo")

        val state = viewModel.uiState.value
        assertNotNull(state.user)
        assertEquals("novo@teste.com", state.user.email)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `register com falha no repositorio mostra erro amigavel`() = runTest {
        authRepository.registerException = Exception("network error: timeout")

        viewModel.register("novo@teste.com", "123456", "Fulano Novo", "", "", "fulanonovo")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Erro de conexão. Verifique sua internet e tente novamente.", state.error)
    }

    // ---------- FORGOT PASSWORD ----------

    @Test
    fun `resetPassword com email em branco mostra erro sem chamar o repositorio`() = runTest {
        viewModel.resetPassword("")

        val state = viewModel.uiState.value
        assertEquals("Digite seu e-mail para recuperar a senha.", state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `resetPassword com sucesso mostra mensagem de confirmacao`() = runTest {
        viewModel.resetPassword(FAKE_USER.email)

        val state = viewModel.uiState.value
        assertEquals("E-mail de recuperação enviado com sucesso!", state.successMessage)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `resetPassword normaliza espacos em branco no email`() = runTest {
        viewModel.resetPassword("  ${FAKE_USER.email}  ")

        assertEquals("E-mail de recuperação enviado com sucesso!", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `resetPassword com falha no envio mostra erro amigavel`() = runTest {
        authRepository.resetPasswordException = Exception("qualquer falha")

        viewModel.resetPassword(FAKE_USER.email)

        val state = viewModel.uiState.value
        assertNull(state.successMessage)
        assertEquals("Erro ao enviar e-mail. Verifique se o e-mail está correto.", state.error)
    }

    // ---------- PROFILE (EDIT DATA) ----------

    @Test
    fun `updateProfile com dados novos e disponiveis atualiza com sucesso`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.updateProfile(name = "Paulo Novo Nome", phone = "11900001111", nickname = "NovoApelido")

        val state = viewModel.uiState.value
        assertEquals("Perfil atualizado com sucesso!", state.successMessage)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Paulo Novo Nome", authRepository.currentUser?.name)
        assertEquals("NovoApelido", authRepository.currentUser?.nickname)
    }

    @Test
    fun `updateProfile mantendo telefone e apelido atuais nao verifica duplicidade`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.updateProfile(name = FAKE_USER.name, phone = FAKE_USER.phone, nickname = FAKE_USER.nickname)

        assertEquals("Perfil atualizado com sucesso!", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `updateProfile com telefone de outro usuario mostra erro`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.updateProfile(name = FAKE_USER.name, phone = FAKE_FRIEND.phone, nickname = FAKE_USER.nickname)

        val state = viewModel.uiState.value
        assertEquals("Este telefone já está em uso.", state.error)
        assertNull(state.successMessage)
        // The phone must remain unchanged on the underlying record
        assertEquals(FAKE_USER.phone, authRepository.currentUser?.phone)
    }

    @Test
    fun `updateProfile com apelido de outro usuario mostra erro`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.updateProfile(name = FAKE_USER.name, phone = FAKE_USER.phone, nickname = FAKE_FRIEND.nickname)

        val state = viewModel.uiState.value
        assertEquals("Este apelido já está em uso.", state.error)
        assertNull(state.successMessage)
    }

    // ---------- USERNAME GENERATION ----------

    @Test
    fun `generateAvailableUsername gera candidato a partir do nome completo`() = runTest {
        val username = viewModel.generateAvailableUsername("Fulano Novo Silva")

        assertTrue(username.isNotBlank())
        assertFalse(authRepository.isUsernameInUse(username))
    }

    @Test
    fun `generateAvailableUsername evita colisao com username existente`() = runTest {
        // FAKE_USER already owns "pauloricha"; generating for the same name must avoid a collision
        val username = viewModel.generateAvailableUsername(FAKE_USER.name)

        assertFalse(username.equals(FAKE_USER.username, ignoreCase = true))
    }
}
