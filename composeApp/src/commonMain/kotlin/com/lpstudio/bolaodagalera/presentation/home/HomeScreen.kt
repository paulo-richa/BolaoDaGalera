package com.lpstudio.bolaodagalera.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.util.getInitials
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onNavigateToBolao: (String) -> Unit,
    onNavigateToCreateBolao: () -> Unit,
    onNavigateToJoinBolao: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val viewModel: HomeViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        NotificationDialog(
            notifications = uiState.notifications,
            onDismiss = { showNotifications = false },
            onAcceptInvitation = { invId, bolaoId ->
                viewModel.respondToInvitation(invId, true)
                showNotifications = false
                onNavigateToBolao(bolaoId)
            },
            onDeclineInvitation = { invId ->
                viewModel.respondToInvitation(invId, false)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Premium Hero Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GradientHero)
                    .drawBehind {
                        // Glossy top light
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                                endY = size.height * 0.5f
                            )
                        )
                        // Neon glow corner
                        drawCircle(
                            brush = Brush.radialGradient(
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
                        val displayName = if (!user?.nickname.isNullOrBlank()) {
                            user.nickname
                        } else {
                            val names = user?.name?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
                            if (names.size >= 2) "${names[0]} ${names[1]}" else names.firstOrNull() ?: "Craque"
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Olá, ",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                displayName,
                                fontSize = 14.sp,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(" 👋", fontSize = 14.sp)
                        }
                    }

                    // User Actions & Profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Notificações
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.4f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, GlassBorder, CircleShape)
                                .clickable { 
                                    showNotifications = true
                                    viewModel.markAllNotificationsAsRead()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notificações",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            
                            // Badge Neon Amarelo com efeito de pulso
                            if (uiState.hasUnreadNotifications) {
                                val neonYellow = Color(0xFFFFF176) // Amarelo Neon vibrante
                                
                                // Círculo de pulso (atrás)
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                            alpha = pulseAlpha
                                        }
                                        .clip(CircleShape)
                                        .background(neonYellow)
                                )

                                // Círculo fixo (frente)
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(neonYellow)
                                        .border(1.dp, DeepNavy, CircleShape)
                                )
                            }
                        }

                        // Avatar com Iniciais
                        val initials = uiState.user?.name?.getInitials() ?: "C"

                        UserAvatar(
                            initials = initials,
                            size = 44.dp,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onNavigateToAccount() }
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            AnimatedContent(
                targetState = uiState.isLoading,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "home_content",
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { loading ->
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Neon, strokeWidth = 2.dp)
                    }
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
                        // 1. Convites Pendentes (Card no topo)
                        if (uiState.invitations.isNotEmpty()) {
                            item(key = "invitations_header") {
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
                                        viewModel.respondToInvitation(invitation.id, true)
                                        onNavigateToBolao(invitation.bolaoId)
                                    },
                                    onDecline = { viewModel.respondToInvitation(invitation.id, false) }
                                )
                            }
                        }

                        // 2. Meus Bolões (Como Admin)
                        if (adminBoloes.isNotEmpty()) {
                            item(key = "admin_header") {
                                Text(
                                    "MEUS BOLÕES (ADMIN)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                )
                            }
                            items(adminBoloes, key = { "admin_${it.id}" }) { bolao ->
                                BolaoCard(bolao = bolao, isAdmin = true, onClick = { onNavigateToBolao(bolao.id) })
                            }
                        }

                        // 3. Bolões que Participo
                        if (participantBoloes.isNotEmpty()) {
                            item(key = "participant_header") {
                                Text(
                                    "BOLÕES QUE PARTICIPO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                )
                            }
                            items(participantBoloes, key = { "part_${it.id}" }) { bolao ->
                                BolaoCard(bolao = bolao, isAdmin = false, onClick = { onNavigateToBolao(bolao.id) })
                            }
                        }
                        
                        // Caso o usuário tenha convites mas não tenha bolões ainda, mostra um incentivo
                        if (uiState.boloes.isEmpty() && uiState.invitations.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(20.dp))
                                OutlinedButton(
                                    onClick = onNavigateToCreateBolao,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(0.3f))
                                ) {
                                    Text("Ou crie o seu próprio bolão 🏆", color = Neon, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            "Sem bolões ainda",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Crie o seu ou entre em um com o código de um amigo",
            fontSize = 14.sp,
            color = TextMuted,
            lineHeight = 20.sp
        )
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
private fun InvitationCard(
    invitation: Invitation,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavyCard)
            .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Volta do ícone de convite que estava bom
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📩", fontSize = 22.sp)
            }
            
            Spacer(Modifier.height(14.dp))

            // Texto unificado: Nome + Convite com :
            Text(
                text = "${invitation.inviterName} te convidou para o bolão:",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(10.dp))
            
            // Nome do Bolão Minimalista
            Text(
                text = invitation.bolaoName.ifBlank { "Copa do Mundo 2026" },
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
                // Botão Recusar
                TextButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Recusar", color = TextMuted, fontSize = 14.sp)
                }
                
                // Botão Aceitar - Destaque Neon
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.3f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Neon)
                ) {
                    Text(
                        "ACEITAR", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Black, 
                        color = DeepNavy,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BolaoCard(bolao: Bolao, isAdmin: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(listOf(NavyElevated, NavyCard))
            )
            .border(1.dp, if (isAdmin) Neon.copy(alpha = 0.3f) else GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        // Left accent line
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isAdmin) GradientPrimary else Brush.verticalGradient(listOf(TextMuted, Color.Transparent)))
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        bolao.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isAdmin) {
                        Text(
                            "ADMIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepNavy,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Neon)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Championship Label
                val champLabel = when(bolao.championshipId) {
                    "AMISTOSOS" -> "Amistosos Pre-Copa ⚽"
                    else -> "Copa do Mundo 2026 🏆"
                }

                // Code (Plain text below title)
                Text(
                    text = "Código: ${bolao.code}",
                    fontSize = 10.sp,
                    color = Gold.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(13.dp), tint = Neon)
                        Text("${bolao.participants.size}", fontSize = 12.sp, color = Neon, fontWeight = FontWeight.SemiBold)
                    }
                    
                    // Championship badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = champLabel,
                            fontSize = 11.sp,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun NotificationDialog(
    notifications: List<com.lpstudio.bolaodagalera.domain.model.Notification>,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(notification.title, color = if (notification.isRead) TextMuted else Neon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (!notification.isRead) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(Neon))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(notification.message, color = if (notification.isRead) TextMuted else Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            
                            if (notification.type == com.lpstudio.bolaodagalera.domain.model.NotificationType.INVITATION && !notification.isRead) {
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
