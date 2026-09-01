package com.lpstudio.bolaodagalera.presentation.auth

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.profile_button_save
import bolaodagalera.composeapp.generated.resources.profile_change_password_dialog_confirm
import bolaodagalera.composeapp.generated.resources.profile_change_password_dialog_dismiss
import bolaodagalera.composeapp.generated.resources.profile_change_password_dialog_message
import bolaodagalera.composeapp.generated.resources.profile_change_password_dialog_title
import bolaodagalera.composeapp.generated.resources.profile_delete_account_dialog_confirm
import bolaodagalera.composeapp.generated.resources.profile_delete_account_dialog_message
import bolaodagalera.composeapp.generated.resources.profile_delete_account_dialog_title
import bolaodagalera.composeapp.generated.resources.profile_field_name_label
import bolaodagalera.composeapp.generated.resources.profile_field_nickname_label
import bolaodagalera.composeapp.generated.resources.profile_field_phone_label
import bolaodagalera.composeapp.generated.resources.profile_field_username_label
import bolaodagalera.composeapp.generated.resources.profile_icon_sign_out_content_description
import bolaodagalera.composeapp.generated.resources.profile_name_error_invalid
import bolaodagalera.composeapp.generated.resources.profile_option_change_password
import bolaodagalera.composeapp.generated.resources.profile_option_delete_account
import bolaodagalera.composeapp.generated.resources.profile_option_help
import bolaodagalera.composeapp.generated.resources.profile_option_invite_friends
import bolaodagalera.composeapp.generated.resources.profile_share_invite_message
import bolaodagalera.composeapp.generated.resources.profile_sign_out_dialog_confirm
import bolaodagalera.composeapp.generated.resources.profile_sign_out_dialog_message
import bolaodagalera.composeapp.generated.resources.profile_sign_out_dialog_title
import bolaodagalera.composeapp.generated.resources.profile_top_bar_title
import bolaodagalera.composeapp.generated.resources.profile_version_label
import com.lpstudio.bolaodagalera.APP_VERSION
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoConfirmDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSnackbarHost
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.components.UserAvatar
import com.lpstudio.bolaodagalera.designsystem.components.rememberBolaoSnackbarHostState
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.theme.TextSubtle
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.util.ValidationUtils
import com.lpstudio.bolaodagalera.util.getInitials
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(onNavigateToHelp: () -> Unit, onNavigateBack: () -> Unit, onSignOut: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val launcherProvider = rememberLauncherProvider()
    val snackbarHostState = rememberBolaoSnackbarHostState()
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(keyboardHeight) {
        // Removido scroll automático para o final em formulários longos
    }

    var name by remember { mutableStateOf(value = "") }
    var nickname by remember { mutableStateOf(value = "") }
    var phone by remember { mutableStateOf(value = "") }
    var showSignOutDialog by remember { mutableStateOf(value = false) }
    var showDeleteAccountDialog by remember { mutableStateOf(value = false) }
    var showChangePasswordDialog by remember { mutableStateOf(value = false) }

    val isNameValid = ValidationUtils.isValidFullName(name)
    val shareInviteMessage = stringResource(Res.string.profile_share_invite_message)

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.user) {
        if (uiState.user == null) {
            onSignOut()
        } else {
            uiState.user?.let {
                name = it.name
                nickname = it.nickname
                phone = it.phone
            }
        }
    }

    if (showSignOutDialog) {
        BolaoConfirmDialog(
            title = stringResource(Res.string.profile_sign_out_dialog_title),
            message = stringResource(Res.string.profile_sign_out_dialog_message),
            confirmText = stringResource(Res.string.profile_sign_out_dialog_confirm),
            isDestructive = true,
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        BolaoConfirmDialog(
            title = stringResource(Res.string.profile_delete_account_dialog_title),
            message = stringResource(Res.string.profile_delete_account_dialog_message),
            confirmText = stringResource(Res.string.profile_delete_account_dialog_confirm),
            isDestructive = true,
            onConfirm = {
                showDeleteAccountDialog = false
                // viewModel.deleteAccount() // A implementar
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showChangePasswordDialog) {
        BolaoDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = NavyCard,
            title = {
                BolaoText(
                    stringResource(Res.string.profile_change_password_dialog_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                BolaoText(
                    stringResource(Res.string.profile_change_password_dialog_message, uiState.user?.email ?: ""),
                    color = TextMuted
                )
            },
            confirmButton = {
                BolaoButton(
                    text = stringResource(Res.string.profile_change_password_dialog_confirm),
                    onClick = {
                        showChangePasswordDialog = false
                        uiState.user?.email?.let { viewModel.resetPassword(it) }
                    }
                )
            },
            dismissButton = {
                BolaoTextButton(onClick = { showChangePasswordDialog = false }) {
                    BolaoText(stringResource(Res.string.profile_change_password_dialog_dismiss), color = TextMuted)
                }
            }
        )
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        BolaoScaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                BolaoSnackbarHost(snackbarHostState)
            },
            topBar = {
                BolaoTopBar(
                    title = stringResource(Res.string.profile_top_bar_title),
                    onNavigateBack = onNavigateBack,
                    actions = {
                        BolaoIconButton(onClick = { showSignOutDialog = true }) {
                            BolaoIcon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                stringResource(Res.string.profile_icon_sign_out_content_description),
                                tint = ErrorRed
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                // Spacer(Modifier.height(10.dp)) // Removido para subir o header ao máximo

                // Avatar large
                UserAvatar(
                    initials = uiState.user?.name?.getInitials() ?: "?",
                    size = 100.dp,
                    fontSize = 40.sp,
                    borderColor = Neon
                )

                Spacer(Modifier.height(16.dp))

                BolaoText(
                    uiState.user?.email ?: "",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(32.dp))

                // Form card
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavyCard)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo ID (Não editável)
                    BolaoTextField(
                        value = uiState.user?.username ?: "",
                        onValueChange = { },
                        label = stringResource(Res.string.profile_field_username_label),
                        enabled = false
                    )

                    BolaoTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(Res.string.profile_field_name_label),
                        isError = name.isNotBlank() && !isNameValid
                    )
                    if (name.isNotBlank() && !isNameValid) {
                        BolaoText(
                            stringResource(Res.string.profile_name_error_invalid),
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    BolaoTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = stringResource(Res.string.profile_field_nickname_label)
                    )

                    BolaoTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = stringResource(Res.string.profile_field_phone_label)
                    )

                    uiState.error?.let {
                        BolaoText(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(8.dp))

                    BolaoButton(
                        text = stringResource(Res.string.profile_button_save),
                        isLoading = uiState.isLoading,
                        enabled = isNameValid && !uiState.isLoading,
                        onClick = { viewModel.updateProfile(name, phone, nickname) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Extra Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileOptionItem(
                        icon = Icons.Default.Share,
                        title = stringResource(Res.string.profile_option_invite_friends)
                    ) {
                        launcherProvider.shareText(shareInviteMessage)
                    }
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = stringResource(Res.string.profile_option_help)
                    ) {
                        onNavigateToHelp()
                    }
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        title = stringResource(Res.string.profile_option_change_password)
                    ) {
                        showChangePasswordDialog = true
                    }
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = stringResource(Res.string.profile_option_delete_account),
                        textColor = ErrorRed
                    ) {
                        showDeleteAccountDialog = true
                    }
                }

                Spacer(Modifier.height(40.dp))

                BolaoText(
                    stringResource(Res.string.profile_version_label, APP_VERSION),
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(Modifier.height(10.dp))
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    BolaoSurface(
        onClick = onClick,
        color = NavyElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BolaoIcon(icon, null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            BolaoText(title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
