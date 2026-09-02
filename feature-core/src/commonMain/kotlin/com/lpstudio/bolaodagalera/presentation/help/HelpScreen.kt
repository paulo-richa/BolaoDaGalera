package com.lpstudio.bolaodagalera.presentation.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_core.generated.resources.Res
import bolaodagalera.feature_core.generated.resources.help_screen_button_send_request
import bolaodagalera.feature_core.generated.resources.help_screen_confirm_dialog_confirm
import bolaodagalera.feature_core.generated.resources.help_screen_confirm_dialog_dismiss
import bolaodagalera.feature_core.generated.resources.help_screen_confirm_dialog_message
import bolaodagalera.feature_core.generated.resources.help_screen_confirm_dialog_title
import bolaodagalera.feature_core.generated.resources.help_screen_error_dialog_confirm
import bolaodagalera.feature_core.generated.resources.help_screen_error_dialog_message
import bolaodagalera.feature_core.generated.resources.help_screen_error_dialog_title
import bolaodagalera.feature_core.generated.resources.help_screen_faq_join_answer
import bolaodagalera.feature_core.generated.resources.help_screen_faq_join_question
import bolaodagalera.feature_core.generated.resources.help_screen_faq_multiple_answer
import bolaodagalera.feature_core.generated.resources.help_screen_faq_multiple_question
import bolaodagalera.feature_core.generated.resources.help_screen_faq_password_answer
import bolaodagalera.feature_core.generated.resources.help_screen_faq_password_question
import bolaodagalera.feature_core.generated.resources.help_screen_faq_points_answer
import bolaodagalera.feature_core.generated.resources.help_screen_faq_points_question
import bolaodagalera.feature_core.generated.resources.help_screen_rule_bullet
import bolaodagalera.feature_core.generated.resources.help_screen_rule_creation_description
import bolaodagalera.feature_core.generated.resources.help_screen_rule_creation_extra
import bolaodagalera.feature_core.generated.resources.help_screen_rule_creation_title
import bolaodagalera.feature_core.generated.resources.help_screen_rule_deadline_description
import bolaodagalera.feature_core.generated.resources.help_screen_rule_deadline_extra
import bolaodagalera.feature_core.generated.resources.help_screen_rule_deadline_title
import bolaodagalera.feature_core.generated.resources.help_screen_rule_scoring_description
import bolaodagalera.feature_core.generated.resources.help_screen_rule_scoring_point_exact
import bolaodagalera.feature_core.generated.resources.help_screen_rule_scoring_point_partial
import bolaodagalera.feature_core.generated.resources.help_screen_rule_scoring_title
import bolaodagalera.feature_core.generated.resources.help_screen_rule_time_description
import bolaodagalera.feature_core.generated.resources.help_screen_rule_time_extra
import bolaodagalera.feature_core.generated.resources.help_screen_rule_time_title
import bolaodagalera.feature_core.generated.resources.help_screen_support_field_placeholder
import bolaodagalera.feature_core.generated.resources.help_screen_support_subtitle
import bolaodagalera.feature_core.generated.resources.help_screen_support_success_subtitle
import bolaodagalera.feature_core.generated.resources.help_screen_support_success_title
import bolaodagalera.feature_core.generated.resources.help_screen_support_title
import bolaodagalera.feature_core.generated.resources.help_screen_tab_faq
import bolaodagalera.feature_core.generated.resources.help_screen_tab_rules
import bolaodagalera.feature_core.generated.resources.help_screen_tab_support
import bolaodagalera.feature_core.generated.resources.help_screen_top_bar_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoConfirmDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLoadingIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val supportRepository = koinInject<SupportRepository>()
    val authRepository = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Auto-dismiss the success state after 3 seconds
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(3000.milliseconds)
            showSuccess = false
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        BolaoConfirmDialog(
            title = stringResource(Res.string.help_screen_confirm_dialog_title),
            message = stringResource(Res.string.help_screen_confirm_dialog_message),
            confirmText = stringResource(Res.string.help_screen_confirm_dialog_confirm),
            dismissText = stringResource(Res.string.help_screen_confirm_dialog_dismiss),
            onConfirm = {
                showConfirmDialog = false
                if (message.isNotBlank() && !isSending) {
                    scope.launch {
                        isSending = true
                        try {
                            val user = authRepository.currentUser
                            supportRepository.sendSupportTicket(
                                userId = user?.id ?: "anonymous",
                                userEmail = user?.email ?: "no-email",
                                message = message
                            )
                            message = "" // Clear the input after a successful send
                            showSuccess = true
                        } catch (_: Exception) {
                            showErrorDialog = true
                        } finally {
                            isSending = false
                        }
                    }
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Error dialog
    if (showErrorDialog) {
        BolaoConfirmDialog(
            title = stringResource(Res.string.help_screen_error_dialog_title),
            message = stringResource(Res.string.help_screen_error_dialog_message),
            confirmText = stringResource(Res.string.help_screen_error_dialog_confirm),
            isDestructive = true,
            onConfirm = { showErrorDialog = false },
            onDismiss = { showErrorDialog = false }
        )
    }

    BolaoScaffold(
        topBar = {
            BolaoTopBar(
                title = stringResource(Res.string.help_screen_top_bar_title),
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = DeepNavy
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Custom tabs
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BolaoSpacing.xl, vertical = BolaoSpacing.lg)
                    .clip(BolaoRadiusShape.md)
                    .background(NavyCard)
                    .padding(BolaoSpacing.xs)
            ) {
                val tabLabels =
                    listOf(
                        stringResource(Res.string.help_screen_tab_rules),
                        stringResource(Res.string.help_screen_tab_faq),
                        stringResource(Res.string.help_screen_tab_support)
                    )
                tabLabels.forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier =
                        Modifier
                            .weight(1f)
                            .clip(BolaoRadiusShape.sm)
                            .background(if (selected) Neon else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = BolaoSpacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        BolaoText(
                            text = label,
                            color = if (selected) DeepNavy else TextMuted,
                            fontSize = BolaoTypography.bodyLarge.fontSize,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> RulesSection()
                    1 -> FaqSection()
                    2 ->
                        SupportSection(
                            message = message,
                            onMessageChange = { message = it },
                            isSending = isSending,
                            showSuccess = showSuccess
                        ) {
                            if (message.isNotBlank()) {
                                showConfirmDialog = true
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun RulesSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.lg)
    ) {
        item {
            RuleCard(
                title = stringResource(Res.string.help_screen_rule_scoring_title),
                description = stringResource(Res.string.help_screen_rule_scoring_description),
                points =
                listOf(
                    stringResource(Res.string.help_screen_rule_scoring_point_exact),
                    stringResource(Res.string.help_screen_rule_scoring_point_partial)
                )
            )
        }
        item {
            RuleCard(
                title = stringResource(Res.string.help_screen_rule_time_title),
                description = stringResource(Res.string.help_screen_rule_time_description),
                extra = stringResource(Res.string.help_screen_rule_time_extra)
            )
        }
        item {
            RuleCard(
                title = stringResource(Res.string.help_screen_rule_deadline_title),
                description = stringResource(Res.string.help_screen_rule_deadline_description),
                extra = stringResource(Res.string.help_screen_rule_deadline_extra)
            )
        }
        item {
            RuleCard(
                title = stringResource(Res.string.help_screen_rule_creation_title),
                description = stringResource(Res.string.help_screen_rule_creation_description),
                extra = stringResource(Res.string.help_screen_rule_creation_extra)
            )
        }
    }
}

@Composable
private fun FaqSection() {
    val faqs =
        listOf(
            stringResource(Res.string.help_screen_faq_join_question) to stringResource(Res.string.help_screen_faq_join_answer),
            stringResource(Res.string.help_screen_faq_points_question) to stringResource(Res.string.help_screen_faq_points_answer),
            stringResource(Res.string.help_screen_faq_multiple_question) to stringResource(Res.string.help_screen_faq_multiple_answer),
            stringResource(Res.string.help_screen_faq_password_question) to stringResource(Res.string.help_screen_faq_password_answer)
        )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
    ) {
        items(faqs) { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            BolaoSurface(
                color = NavyElevated,
                shape = BolaoRadiusShape.lg,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(BolaoSpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BolaoText(
                            text = question,
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            fontSize = BolaoTypography.titleLarge.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                        BolaoIcon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Neon
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        BolaoText(
                            text = answer,
                            color = TextMuted,
                            fontSize = BolaoTypography.bodyLarge.fontSize,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = BolaoSpacing.md)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportSection(
    message: String,
    onMessageChange: (String) -> Unit,
    isSending: Boolean,
    showSuccess: Boolean,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(BolaoSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSuccess) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BolaoIcon(Icons.Default.CheckCircle, null, tint = Neon, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    BolaoText(
                        stringResource(Res.string.help_screen_support_success_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    BolaoText(
                        stringResource(Res.string.help_screen_support_success_subtitle),
                        color = TextMuted,
                        fontSize = BolaoTypography.bodyLarge.fontSize
                    )
                }
            }
        } else {
            Box(
                modifier =
                Modifier
                    .size(64.dp)
                    .clip(BolaoRadiusShape.xl)
                    .background(Neon.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                BolaoIcon(Icons.Default.MailOutline, contentDescription = null, tint = Neon, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.height(20.dp))

            BolaoText(
                stringResource(Res.string.help_screen_support_title),
                color = Color.White,
                fontSize = BolaoTypography.headlineSmall.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            BolaoText(
                stringResource(Res.string.help_screen_support_subtitle),
                color = TextMuted,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = BolaoSpacing.sm, bottom = BolaoSpacing.xxl),
                lineHeight = 20.sp
            )

            BolaoTextField(
                value = message,
                onValueChange = onMessageChange,
                label = stringResource(Res.string.help_screen_support_field_placeholder),
                enabled = !isSending,
                modifier = Modifier.height(160.dp),
                singleLine = false,
                minLines = 4
            )

            Spacer(Modifier.height(24.dp))

            if (isSending) {
                BolaoLoadingIndicator()
            } else {
                BolaoButton(
                    text = stringResource(Res.string.help_screen_button_send_request),
                    onClick = onSend
                )
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, description: String, points: List<String> = emptyList(), extra: String? = null) {
    BolaoSurface(
        color = NavyElevated,
        shape = BolaoRadiusShape.lg,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(BolaoSpacing.lg)) {
            BolaoText(title, color = Neon, fontSize = BolaoTypography.titleLarge.fontSize, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BolaoText(description, color = Color.White, fontSize = BolaoTypography.bodyLarge.fontSize, lineHeight = 20.sp)

            if (points.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                points.forEach { point ->
                    Row(modifier = Modifier.padding(vertical = BolaoSpacing.xs)) {
                        BolaoText(
                            stringResource(Res.string.help_screen_rule_bullet),
                            color = Gold,
                            modifier = Modifier.padding(end = BolaoSpacing.sm)
                        )
                        BolaoText(point, color = TextMuted, fontSize = BolaoTypography.bodyLarge.fontSize, lineHeight = 18.sp)
                    }
                }
            }

            if (extra != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(BolaoRadiusShape.sm)
                        .background(Gold.copy(alpha = 0.1f))
                        .padding(BolaoSpacing.md)
                ) {
                    BolaoText(extra, color = Gold, fontSize = BolaoTypography.bodyMedium.fontSize, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
