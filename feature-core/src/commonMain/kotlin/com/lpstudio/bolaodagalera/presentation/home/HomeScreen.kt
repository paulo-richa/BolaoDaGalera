package com.lpstudio.bolaodagalera.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_core.generated.resources.Res
import bolaodagalera.feature_core.generated.resources.home_screen_admin_badge
import bolaodagalera.feature_core.generated.resources.home_screen_app_title
import bolaodagalera.feature_core.generated.resources.home_screen_bolao_code_label
import bolaodagalera.feature_core.generated.resources.home_screen_button_accept
import bolaodagalera.feature_core.generated.resources.home_screen_button_accept_caps
import bolaodagalera.feature_core.generated.resources.home_screen_button_create_bolao
import bolaodagalera.feature_core.generated.resources.home_screen_button_decline
import bolaodagalera.feature_core.generated.resources.home_screen_button_join_with_code
import bolaodagalera.feature_core.generated.resources.home_screen_empty_subtitle
import bolaodagalera.feature_core.generated.resources.home_screen_empty_title
import bolaodagalera.feature_core.generated.resources.home_screen_empty_trophy_emoji
import bolaodagalera.feature_core.generated.resources.home_screen_error_snackbar_dismiss
import bolaodagalera.feature_core.generated.resources.home_screen_greeting_prefix
import bolaodagalera.feature_core.generated.resources.home_screen_greeting_wave_emoji
import bolaodagalera.feature_core.generated.resources.home_screen_invitation_emoji
import bolaodagalera.feature_core.generated.resources.home_screen_invitation_message
import bolaodagalera.feature_core.generated.resources.home_screen_notification_bell_emoji
import bolaodagalera.feature_core.generated.resources.home_screen_notification_dialog_close
import bolaodagalera.feature_core.generated.resources.home_screen_notification_dialog_title
import bolaodagalera.feature_core.generated.resources.home_screen_notification_empty
import bolaodagalera.feature_core.generated.resources.home_screen_notifications_cd
import bolaodagalera.feature_core.generated.resources.home_screen_section_admin_boloes
import bolaodagalera.feature_core.generated.resources.home_screen_section_participant_boloes
import bolaodagalera.feature_core.generated.resources.home_screen_section_pending_invitations
import com.lpstudio.bolaodagalera.ads.AdBannerProvider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLoadingIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoOutlinedButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.UserAvatar
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientHero
import com.lpstudio.bolaodagalera.designsystem.theme.GradientPrimary
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.NotificationType
import com.lpstudio.bolaodagalera.util.getInitials
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToBolao: (String) -> Unit,
    onNavigateToCreateBolao: () -> Unit,
    onNavigateToJoinBolao: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val adBannerProvider = koinInject<AdBannerProvider>()
    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        NotificationDialog(
            notifications = uiState.notifications,
            onDismiss = { showNotifications = false },
            onAcceptInvitation = { invId, bolaoId ->
                viewModel.respondToInvitation(invId, true) {
                    showNotifications = false
                    onNavigateToBolao(bolaoId)
                }
            },
            onDeclineInvitation = { invId ->
                viewModel.respondToInvitation(invId, false)
            }
        )
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        uiState.error?.let {
            BolaoSurface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
                color = ErrorRed,
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BolaoText(it, color = Color.White, modifier = Modifier.weight(1f))
                    BolaoTextButton(onClick = { viewModel.clearError() }) {
                        BolaoText(
                            stringResource(Res.string.home_screen_error_snackbar_dismiss),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(Modifier.fillMaxSize()) {
            // ── Premium Hero Header ──────────────────────────────────────────
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .background(GradientHero)
                    .drawBehind {
                        drawRect(
                            brush =
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                                endY = size.height * 0.5f
                            )
                        )
                        drawCircle(
                            brush =
                            Brush.radialGradient(
                                colors = listOf(Neon.copy(alpha = 0.15f), Color.Transparent),
                                center = Offset(size.width * 0.9f, 0f),
                                radius = 220.dp.toPx()
                            ),
                            radius = 220.dp.toPx(),
                            center = Offset(size.width * 0.9f, 0f)
                        )
                    }
                    .padding(top = 12.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BolaoText(
                            stringResource(Res.string.home_screen_app_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(Modifier.height(4.dp))

                        val user = uiState.user
                        val displayName =
                            if (!user?.nickname.isNullOrBlank()) {
                                user.nickname
                            } else {
                                val names = user?.name?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
                                if (names.size >= 2) "${names[0]} ${names[1]}" else names.firstOrNull() ?: "Craque"
                            }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BolaoText(
                                stringResource(Res.string.home_screen_greeting_prefix),
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            BolaoText(displayName, fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Bold)
                            BolaoText(stringResource(Res.string.home_screen_greeting_wave_emoji), fontSize = 14.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.4f,
                            animationSpec =
                            infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 0f,
                            animationSpec =
                            infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        Box(
                            modifier =
                            Modifier.size(
                                44.dp
                            ).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).border(1.dp, GlassBorder, CircleShape)
                                .clickable {
                                    showNotifications = true
                                    viewModel.markAllNotificationsAsRead()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            BolaoIcon(
                                Icons.Outlined.Notifications,
                                stringResource(Res.string.home_screen_notifications_cd),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            if (uiState.hasUnreadNotifications) {
                                val neonYellow = Color(0xFFFFF176)
                                Box(
                                    modifier =
                                    Modifier.size(12.dp).align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp).graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = pulseAlpha
                                    }.clip(CircleShape).background(neonYellow)
                                )
                                Box(
                                    modifier =
                                    Modifier.size(
                                        8.dp
                                    ).align(
                                        Alignment.TopEnd
                                    ).offset(
                                        x = (-4).dp,
                                        y = 4.dp
                                    ).clip(CircleShape).background(neonYellow).border(1.dp, DeepNavy, CircleShape)
                                )
                            }
                        }

                        UserAvatar(
                            initials = uiState.user?.name?.getInitials() ?: "C",
                            size = 44.dp,
                            fontSize = 16.sp,
                            modifier =
                            Modifier.clickable {
                                onNavigateToAccount()
                            }
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "home_content"
                ) { loading ->
                    if (loading) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { BolaoLoadingIndicator() }
                    } else if (uiState.boloes.isEmpty() && uiState.invitations.isEmpty()) {
                        EmptyState(
                            modifier = Modifier.fillMaxSize(),
                            onCreateClick = onNavigateToCreateBolao,
                            onJoinClick = onNavigateToJoinBolao
                        )
                    } else {
                        val currentUserId = uiState.user?.id
                        val (adminBoloes, participantBoloes) = uiState.boloes.partition { it.ownerId == currentUserId }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (uiState.invitations.isNotEmpty()) {
                                item {
                                    BolaoText(
                                        stringResource(Res.string.home_screen_section_pending_invitations, uiState.invitations.size),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(uiState.invitations, key = { "inv_${it.id}" }) { invitation ->
                                    InvitationCard(
                                        invitation = invitation,
                                        onAccept = {
                                            viewModel.respondToInvitation(invitation.id, true) {
                                                onNavigateToBolao(invitation.bolaoId)
                                            }
                                        },
                                        onDecline = { viewModel.respondToInvitation(invitation.id, false) }
                                    )
                                }
                            }
                            if (adminBoloes.isNotEmpty()) {
                                item {
                                    BolaoText(
                                        stringResource(Res.string.home_screen_section_admin_boloes),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                    )
                                }
                                items(adminBoloes, key = {
                                    "admin_${it.id}"
                                }) { bolao -> BolaoCard(bolao = bolao, isAdmin = true, onClick = { onNavigateToBolao(bolao.id) }) }
                            }
                            if (participantBoloes.isNotEmpty()) {
                                item {
                                    BolaoText(
                                        stringResource(Res.string.home_screen_section_participant_boloes),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.5.sp,
                                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                    )
                                }
                                items(participantBoloes, key = {
                                    "part_${it.id}"
                                }) { bolao -> BolaoCard(bolao = bolao, isAdmin = false, onClick = { onNavigateToBolao(bolao.id) }) }
                            }
                        }
                    }
                }
            }

            // ── Banner Ad ────────────────────────────────────────────────────
            adBannerProvider.Banner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy))
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, onCreateClick: () -> Unit, onJoinClick: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BolaoText(stringResource(Res.string.home_screen_empty_trophy_emoji), fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        BolaoText(stringResource(Res.string.home_screen_empty_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        BolaoText(stringResource(Res.string.home_screen_empty_subtitle), fontSize = 14.sp, color = TextMuted, lineHeight = 20.sp)
        Spacer(Modifier.height(32.dp))
        BolaoButton(text = stringResource(Res.string.home_screen_button_create_bolao), onClick = onCreateClick)
        Spacer(Modifier.height(12.dp))
        BolaoOutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
            contentColor = Neon
        ) {
            BolaoText(
                stringResource(Res.string.home_screen_button_join_with_code),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun InvitationCard(invitation: Invitation, onAccept: () -> Unit, onDecline: () -> Unit) {
    val fallbackTitle = stringResource(Res.string.home_screen_app_title)
    Box(
        modifier =
        Modifier.fillMaxWidth().clip(
            RoundedCornerShape(20.dp)
        ).background(NavyCard).border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Gold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                BolaoText(stringResource(Res.string.home_screen_invitation_emoji), fontSize = 22.sp)
            }
            Spacer(Modifier.height(14.dp))
            BolaoText(
                text = stringResource(Res.string.home_screen_invitation_message, invitation.inviterName),
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(10.dp))
            BolaoText(
                text = invitation.bolaoName.ifBlank { fallbackTitle },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BolaoTextButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { BolaoText(stringResource(Res.string.home_screen_button_decline), color = TextMuted, fontSize = 14.sp) }
                BolaoButton(
                    text = stringResource(Res.string.home_screen_button_accept_caps),
                    onClick = onAccept,
                    modifier = Modifier.weight(1.3f).height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun BolaoCard(bolao: Bolao, isAdmin: Boolean, onClick: () -> Unit) {
    Box(
        modifier =
        Modifier.fillMaxWidth().clip(
            RoundedCornerShape(18.dp)
        ).background(
            Brush.linearGradient(listOf(NavyElevated, NavyCard))
        ).border(
            1.dp,
            if (isAdmin) Neon.copy(alpha = 0.3f) else GlassBorder,
            RoundedCornerShape(18.dp)
        ).clickable(onClick = onClick).padding(18.dp)
    ) {
        Box(
            modifier =
            Modifier.width(
                3.dp
            ).height(
                48.dp
            ).clip(
                RoundedCornerShape(2.dp)
            ).background(
                if (isAdmin) GradientPrimary else Brush.verticalGradient(listOf(TextMuted, Color.Transparent))
            ).align(Alignment.CenterStart)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BolaoText(
                        bolao.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAdmin) {
                        BolaoText(
                            stringResource(Res.string.home_screen_admin_badge),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepNavy,
                            modifier =
                            Modifier.padding(
                                top = 4.dp
                            ).clip(RoundedCornerShape(4.dp)).background(Neon).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                BolaoText(
                    text = stringResource(Res.string.home_screen_bolao_code_label, bolao.code),
                    fontSize = 10.sp,
                    color = Gold.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BolaoIcon(Icons.Default.Person, null, modifier = Modifier.size(13.dp), tint = Neon)
                        BolaoText(
                            bolao.participants.size.toString(),
                            fontSize = 12.sp,
                            color = Neon,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier =
                        Modifier.clip(
                            RoundedCornerShape(6.dp)
                        ).background(Gold.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        BolaoText(
                            text = Championship.fromId(bolao.championshipId).displayName,
                            fontSize = 11.sp,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            BolaoIcon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSubtle, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun NotificationDialog(
    notifications: List<Notification>,
    onAcceptInvitation: (String, String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val declineText = stringResource(Res.string.home_screen_button_decline)
    val acceptText = stringResource(Res.string.home_screen_button_accept)
    BolaoDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BolaoText(
                    stringResource(Res.string.home_screen_notification_dialog_title),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.weight(1f))
                BolaoText(stringResource(Res.string.home_screen_notification_bell_emoji), fontSize = 18.sp)
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    BolaoText(
                        stringResource(Res.string.home_screen_notification_empty),
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notification ->
                        val bgColor = if (notification.isRead) NavyCard.copy(alpha = 0.6f) else NavyElevated
                        val borderColor = if (notification.isRead) Color.Transparent else Neon.copy(alpha = 0.2f)
                        Column(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BolaoText(
                                    notification.title,
                                    color = if (notification.isRead) TextMuted else Neon,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (!notification.isRead) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(Neon))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            BolaoText(
                                notification.message,
                                color = if (notification.isRead) TextMuted else Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            if ((notification.type == NotificationType.INVITATION) && !notification.isRead) {
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BolaoOutlinedButton(
                                        onClick = { onDeclineInvitation(notification.id.removePrefix("invitation_")) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        BolaoText(declineText, fontSize = 12.sp, color = TextMuted)
                                    }
                                    BolaoButton(
                                        text = acceptText,
                                        onClick = {
                                            notification.bolaoId?.let {
                                                onAcceptInvitation(notification.id.removePrefix("invitation_"), it)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            BolaoTextButton(onClick = onDismiss) {
                BolaoText(stringResource(Res.string.home_screen_notification_dialog_close), color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    )
}
