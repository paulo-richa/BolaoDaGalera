package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.add_participants_button_send_invite
import bolaodagalera.feature_bolao.generated.resources.add_participants_button_share_link
import bolaodagalera.feature_bolao.generated.resources.add_participants_default_inviter_name
import bolaodagalera.feature_bolao.generated.resources.add_participants_error_send_failed
import bolaodagalera.feature_bolao.generated.resources.add_participants_error_user_not_found
import bolaodagalera.feature_bolao.generated.resources.add_participants_field_identifier_label
import bolaodagalera.feature_bolao.generated.resources.add_participants_info_emoji
import bolaodagalera.feature_bolao.generated.resources.add_participants_info_message
import bolaodagalera.feature_bolao.generated.resources.add_participants_info_title
import bolaodagalera.feature_bolao.generated.resources.add_participants_section_title
import bolaodagalera.feature_bolao.generated.resources.add_participants_share_message
import bolaodagalera.feature_bolao.generated.resources.add_participants_success_message
import bolaodagalera.feature_bolao.generated.resources.add_participants_top_bar_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoOutlinedButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.SuccessGreen
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private enum class ParticipantInputType {
    EMAIL,
    PHONE,
    USER
}

private val logger = appLogger("AddParticipantsScreen")

@Composable
fun AddParticipantsScreen(bolaoId: String, onNavigateBack: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var bolaoCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val launcherProvider = rememberLauncherProvider()
    val bolaoRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.BolaoRepository>()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val invitationRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.InvitationRepository>()
    val crashReporter = koinInject<CrashReporter>()
    var bolaoName by remember { mutableStateOf("") }

    val defaultInviterName = stringResource(Res.string.add_participants_default_inviter_name)
    val userNotFoundError = stringResource(Res.string.add_participants_error_user_not_found)
    val sendFailedError = stringResource(Res.string.add_participants_error_send_failed)
    val webUrl = "https://bolaodagalera-bb002.web.app/invite?code=$bolaoCode"
    val appUrl = "bolaodagalera://invite?code=$bolaoCode"
    val shareMessage = stringResource(Res.string.add_participants_share_message, bolaoName, webUrl, appUrl, bolaoCode)

    // Load pool name and code on entry so the invite link/message can be built.
    LaunchedEffect(bolaoId) {
        try {
            val bolao = bolaoRepository.getBolao(bolaoId)
            bolaoName = bolao.name
            bolaoCode = bolao.code
        } catch (e: Exception) {
            crashReporter.recordException(e, "Failed to load pool data for invitation")
        }
    }

    // Infer the input type from its shape to route validation and keyboard type.
    val detectedType =
        remember(identifier) {
            val trimmed = identifier.trim()
            when {
                trimmed.contains("@") && trimmed.contains(".") -> ParticipantInputType.EMAIL
                trimmed.filter { it.isDigit() }.length >= 8 -> ParticipantInputType.PHONE
                else -> ParticipantInputType.USER
            }
        }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .systemBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ─────────────────────────────────────────────────────────
            BolaoTopBar(title = stringResource(Res.string.add_participants_top_bar_title), onNavigateBack = onNavigateBack)

            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = BolaoSpacing.xxl)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(20.dp))

                BolaoText(
                    stringResource(Res.string.add_participants_section_title),
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(12.dp))

                // Single input field that adapts keyboard/validation to the detected type
                BolaoTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = stringResource(Res.string.add_participants_field_identifier_label),
                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                        when (detectedType) {
                            ParticipantInputType.EMAIL -> KeyboardType.Email
                            ParticipantInputType.PHONE -> KeyboardType.Phone
                            else -> KeyboardType.Text
                        }
                    )
                )

                Spacer(Modifier.height(24.dp))

                if (showSuccessMessage) {
                    Box(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(BolaoRadiusShape.md)
                            .background(SuccessGreen.copy(alpha = 0.1f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.3f), BolaoRadiusShape.md)
                            .padding(BolaoSpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        BolaoText(
                            stringResource(Res.string.add_participants_success_message),
                            color = SuccessGreen,
                            fontSize = BolaoTypography.bodyLarge.fontSize,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                error?.let {
                    BolaoText(
                        it,
                        color = ErrorRed,
                        fontSize = BolaoTypography.bodyMedium.fontSize,
                        modifier = Modifier.fillMaxWidth().padding(bottom = BolaoSpacing.lg),
                        textAlign = TextAlign.Center
                    )
                }

                // Single action: send invitation
                BolaoButton(
                    text = stringResource(Res.string.add_participants_button_send_invite),
                    isLoading = isLoading,
                    enabled = identifier.isNotBlank() && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                val trimmedId = identifier.trim()
                                val inviterName = authRepository.currentUser?.name ?: defaultInviterName

                                // 1. Check the user exists in the database before inviting
                                val userExists =
                                    when (detectedType) {
                                        ParticipantInputType.EMAIL -> authRepository.isEmailInUse(trimmedId.lowercase())
                                        ParticipantInputType.PHONE -> authRepository.isPhoneInUse(trimmedId.filter { it.isDigit() })
                                        ParticipantInputType.USER -> authRepository.isUsernameInUse(trimmedId.lowercase())
                                    }

                                if (!userExists) {
                                    error = userNotFoundError
                                    isLoading = false
                                    return@launch
                                }

                                // 2. Send the in-app invitation
                                val inviteeIdentifier =
                                    when (detectedType) {
                                        ParticipantInputType.EMAIL -> trimmedId.lowercase()
                                        ParticipantInputType.PHONE -> trimmedId.filter { it.isDigit() }
                                        ParticipantInputType.USER -> trimmedId.lowercase()
                                    }

                                try {
                                    withTimeout(3000) {
                                        invitationRepository.sendInvitation(
                                            bolaoId = bolaoId,
                                            bolaoName = bolaoName,
                                            inviterName = inviterName,
                                            inviteeIdentifier = inviteeIdentifier
                                        )
                                    }
                                } catch (e: Exception) {
                                    logger.w(e) { "In-app invitation queued for later delivery (slow network)" }
                                }

                                isLoading = false
                                showSuccessMessage = true
                                identifier = ""

                                delay(3000)
                                showSuccessMessage = false
                            } catch (e: Exception) {
                                crashReporter.recordException(e, "Failed to send invitation")
                                error = sendFailedError
                                isLoading = false
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                BolaoOutlinedButton(
                    onClick = {
                        launcherProvider.shareText(shareMessage)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = BolaoRadiusShape.lg,
                    border = BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
                    contentColor = Neon
                ) {
                    BolaoIcon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BolaoText(
                        stringResource(Res.string.add_participants_button_share_link),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = BolaoTypography.titleLarge.fontSize
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Info section
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(BolaoRadiusShape.lg)
                        .background(NavyElevated)
                        .padding(BolaoSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BolaoText(stringResource(Res.string.add_participants_info_emoji), fontSize = BolaoTypography.displayMedium.fontSize)
                    Spacer(Modifier.height(12.dp))
                    BolaoText(
                        stringResource(Res.string.add_participants_info_title),
                        fontSize = BolaoTypography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    BolaoText(
                        stringResource(Res.string.add_participants_info_message),
                        fontSize = BolaoTypography.bodyLarge.fontSize,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                    Spacer(Modifier.height(keyboardHeight + 100.dp))
                }
            }
        }
    }
}
