package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Immutable
data class JoinBolaoUiState(
    val isLoading: Boolean = false,
    val joinedBolao: Bolao? = null,
    val error: String? = null
)

class JoinBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinBolaoUiState())
    val uiState: StateFlow<JoinBolaoUiState> = _uiState.asStateFlow()

    fun join(code: String) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Mudança de requestJoinBolao para joinBolao (entrada direta via código)
                val bolao = bolaoRepository.joinBolao(code.trim().uppercase(), userId)
                _uiState.update { it.copy(joinedBolao = bolao, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Código inválido.", isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinBolaoScreen(
    initialCode: String = "",
    onJoined: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val bolaoRepository = koinInject<BolaoRepository>()
    val authRepository = koinInject<AuthRepository>()
    val viewModel = remember { JoinBolaoViewModel(bolaoRepository, authRepository) }
    val uiState by viewModel.uiState.collectAsState()
    var code by remember { mutableStateOf(initialCode) }
    var codeTouched by remember { mutableStateOf(initialCode.isNotEmpty()) }
    
    val codeError = if (codeTouched && code.length < 6) "Código deve ter 6 caracteres" else null

    LaunchedEffect(initialCode) {
        if (initialCode.length == 6) {
            viewModel.join(initialCode)
        }
    }

    LaunchedEffect(uiState.joinedBolao) {
        uiState.joinedBolao?.let { onJoined(it.id) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBg)
    ) {
        // Glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("Entrar em Bolão", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🔑", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Digite o código do bolão",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Peça o código de 6 caracteres para quem criou o bolão.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(36.dp))

                // Code input card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { 
                                if (it.length <= 6) code = it.uppercase()
                                codeTouched = true
                            },
                            label = { Text("Código", color = if (codeError != null) ErrorRed else TextMuted, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = codeError != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Color(0xFF2A3D55),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Gold,
                                focusedContainerColor = NavyElevated,
                                unfocusedContainerColor = NavyCard,
                                errorBorderColor = ErrorRed,
                                errorLabelColor = ErrorRed,
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { if (code.length == 6) viewModel.join(code) }
                            ),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 10.sp,
                                textAlign = TextAlign.Center,
                                color = Gold
                            )
                        )

                        codeError?.let { Text(it, color = ErrorRed, fontSize = 11.sp) }

                        // Char counter dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 6) {
                                val filled = i < code.length
                                Box(
                                    modifier = Modifier
                                        .size(if (filled) 10.dp else 8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (filled) Gold else NavyElevated)
                                        .border(1.dp, if (filled) Gold else GlassBorder, RoundedCornerShape(50))
                                )
                            }
                        }

                        uiState.error?.let {
                            Text(it, color = ErrorRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                BolaoButton(
                    text = "Entrar no Bolão",
                    isLoading = uiState.isLoading,
                    enabled = code.length == 6 && !uiState.isLoading,
                    gradient = GradientGold,
                    onClick = { viewModel.join(code) }
                )
            }
        }
    }
}
