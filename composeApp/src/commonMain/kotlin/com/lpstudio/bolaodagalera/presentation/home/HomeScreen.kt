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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lpstudio.bolaodagalera.ADMOB_ANDROID_BANNER_ID
import com.lpstudio.bolaodagalera.ADMOB_IOS_BANNER_ID
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.NotificationType
import com.lpstudio.bolaodagalera.getPlatform
import com.lpstudio.bolaodagalera.presentation.components.AdBanner
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.GradientHero
import com.lpstudio.bolaodagalera.presentation.theme.GradientPrimary
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.theme.TextSubtle
import com.lpstudio.bolaodagalera.util.getInitials
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
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
                containerColor = ErrorRed,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Text(it)
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
                        Text(
                            "Bolão da Galera",
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
                            Text("Olá, ", fontSize = 14.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                            Text(displayName, fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Bold)
                            Text(" 👋", fontSize = 14.sp)
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
                            Icon(Icons.Outlined.Notifications, "Notificações", tint = Color.White, modifier = Modifier.size(22.dp))
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
                        ) { CircularProgressIndicator(color = Neon, strokeWidth = 2.dp) }
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
                                    Text(
                                        "CONVITES PENDENTES (${uiState.invitations.size})",
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
                                    Text(
                                        "MEUS BOLÕES (ADMIN)",
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
                                    Text(
                                        "BOLÕES QUE PARTICIPO",
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
            val platform = getPlatform().name.lowercase()
            val adId = if (platform.contains("android")) ADMOB_ANDROID_BANNER_ID else ADMOB_IOS_BANNER_ID
            AdBanner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy), adId = adId)
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
        Text("🏆", fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text("Sem bolões ainda", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Crie o seu ou entre em um com o código de um amigo", fontSize = 14.sp, color = TextMuted, lineHeight = 20.sp)
        Spacer(Modifier.height(32.dp))
        BolaoButton(text = "Criar bolão", onClick = onCreateClick)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Neon)
        ) {
            Text("Entrar com código", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun InvitationCard(invitation: Invitation, onAccept: () -> Unit, onDecline: () -> Unit) {
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
                Text("📩", fontSize = 22.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "${invitation.inviterName} te convidou para o bolão:",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text =
                invitation.bolaoName.ifBlank {
                    "Bolão da Galera"
                },
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
                TextButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text("Recusar", color = TextMuted, fontSize = 14.sp) }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.3f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Neon)
                ) {
                    Text("ACEITAR", fontSize = 14.sp, fontWeight = FontWeight.Black, color = DeepNavy, letterSpacing = 1.sp)
                }
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
                    Text(bolao.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    if (isAdmin) {
                        Text(
                            "ADMIN",
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
                Text(
                    text = "Código: ${bolao.code}",
                    fontSize = 10.sp,
                    color = Gold.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(13.dp), tint = Neon)
                        Text(bolao.participants.size.toString(), fontSize = 12.sp, color = Neon, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier =
                        Modifier.clip(
                            RoundedCornerShape(6.dp)
                        ).background(Gold.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = Championship.fromId(bolao.championshipId).displayName,
                            fontSize = 11.sp,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSubtle, modifier = Modifier.size(22.dp))
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
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notificações", color = Color.White, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Text("🔔", fontSize = 18.sp)
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("Tudo em dia! Sem alertas por enquanto.", color = TextMuted, textAlign = TextAlign.Center)
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
                                Text(
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
                            Text(
                                notification.message,
                                color = if (notification.isRead) TextMuted else Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            if ((notification.type == NotificationType.INVITATION) && !notification.isRead) {
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onDeclineInvitation(notification.id.removePrefix("invitation_")) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Text("Recusar", fontSize = 12.sp, color = TextMuted)
                                    }
                                    Button(
                                        onClick = {
                                            notification.bolaoId?.let {
                                                onAcceptInvitation(notification.id.removePrefix("invitation_"), it)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Neon)
                                    ) {
                                        Text("Aceitar", fontSize = 12.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("FECHAR", color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    )
}
