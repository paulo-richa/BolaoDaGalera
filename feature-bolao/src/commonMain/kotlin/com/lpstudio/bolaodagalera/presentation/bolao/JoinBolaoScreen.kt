package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.join_bolao_button_join
import bolaodagalera.feature_bolao.generated.resources.join_bolao_code_error_length
import bolaodagalera.feature_bolao.generated.resources.join_bolao_field_code_label
import bolaodagalera.feature_bolao.generated.resources.join_bolao_key_emoji
import bolaodagalera.feature_bolao.generated.resources.join_bolao_request_sent_confirm
import bolaodagalera.feature_bolao.generated.resources.join_bolao_request_sent_message
import bolaodagalera.feature_bolao.generated.resources.join_bolao_request_sent_title
import bolaodagalera.feature_bolao.generated.resources.join_bolao_subtitle
import bolaodagalera.feature_bolao.generated.resources.join_bolao_title
import bolaodagalera.feature_bolao.generated.resources.join_bolao_top_bar_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoGlassCard
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientBg
import com.lpstudio.bolaodagalera.designsystem.theme.GradientGold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JoinBolaoScreen(initialCode: String = "", onJoined: (String) -> Unit, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<JoinBolaoViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    var code by remember(initialCode) { mutableStateOf(initialCode) }
    var codeTouched by remember(initialCode) { mutableStateOf(initialCode.isNotEmpty()) }

    val codeError = if (codeTouched && code.length < 6) stringResource(Res.string.join_bolao_code_error_length) else null

    LaunchedEffect(initialCode) {
        if (initialCode.length == 6) {
            viewModel.join(initialCode)
        }
    }

    LaunchedEffect(uiState.alreadyMemberBolaoId) {
        uiState.alreadyMemberBolaoId?.let { bolaoId ->
            onJoined(bolaoId)
        }
    }

    if (uiState.requestSent) {
        BolaoDialog(
            onDismissRequest = onNavigateBack,
            containerColor = DeepNavy,
            title = {
                BolaoText(stringResource(Res.string.join_bolao_request_sent_title), color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                BolaoText(stringResource(Res.string.join_bolao_request_sent_message), color = TextMuted)
            },
            confirmButton = {
                BolaoButton(
                    text = stringResource(Res.string.join_bolao_request_sent_confirm),
                    onClick = onNavigateBack
                )
            }
        )
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(GradientBg)
    ) {
        // Glow
        Box(
            modifier =
            Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        BolaoScaffold(
            containerColor = Color.Transparent,
            topBar = {
                BolaoTopBar(title = stringResource(Res.string.join_bolao_top_bar_title), onNavigateBack = onNavigateBack)
            }
        ) { padding ->
            val scrollState = rememberScrollState()
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = BolaoSpacing.xxxl)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BolaoText(stringResource(Res.string.join_bolao_key_emoji), fontSize = BolaoTypography.displayLarge.fontSize)
                Spacer(Modifier.height(16.dp))
                BolaoText(
                    stringResource(Res.string.join_bolao_title),
                    fontSize = BolaoTypography.headlineLarge.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                BolaoText(
                    stringResource(Res.string.join_bolao_subtitle),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(36.dp))

                // Code input card
                BolaoGlassCard(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(BolaoSpacing.lg)
                ) {
                    BolaoTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 6) code = it.uppercase()
                            codeTouched = true
                        },
                        label = stringResource(Res.string.join_bolao_field_code_label),
                        isError = codeError != null,
                        accentColor = Gold,
                        keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions =
                        KeyboardActions(
                            onDone = { if (code.length == 6) viewModel.join(code) }
                        ),
                        textStyle =
                        TextStyle(
                            fontSize = BolaoTypography.displayMedium.fontSize,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Gold
                        )
                    )

                    codeError?.let { BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize) }

                    // Char counter dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val filled = i < code.length
                            Box(
                                modifier =
                                Modifier
                                    .size(if (filled) 10.dp else 8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (filled) Gold else NavyElevated)
                                    .border(1.dp, if (filled) Gold else GlassBorder, RoundedCornerShape(50))
                            )
                        }
                    }

                    uiState.error?.let {
                        BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize, textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(24.dp))

                BolaoButton(
                    text = stringResource(Res.string.join_bolao_button_join),
                    isLoading = uiState.isLoading,
                    enabled = code.length == 6 && !uiState.isLoading,
                    gradient = GradientGold,
                    onClick = { viewModel.join(code) }
                )

                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}
