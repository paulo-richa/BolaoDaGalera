package com.lpstudio.bolaodagalera.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.main_screen_fab_ball_emoji
import bolaodagalera.composeapp.generated.resources.main_screen_fab_create_bolao
import bolaodagalera.composeapp.generated.resources.main_screen_fab_join_with_code
import bolaodagalera.composeapp.generated.resources.main_screen_tab_boloes
import bolaodagalera.composeapp.generated.resources.main_screen_tab_conta
import com.lpstudio.bolaodagalera.CommonBackHandler
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.auth.ProfileScreen
import com.lpstudio.bolaodagalera.presentation.home.HomeScreen
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainScreen(
    onNavigateToBolao: (String) -> Unit,
    onNavigateToCreateBolao: () -> Unit,
    onNavigateToJoinBolao: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFabMenu by remember { mutableStateOf(false) }

    // Handle system back button to return to Home from Account
    CommonBackHandler(enabled = selectedTab != 0 || showFabMenu) {
        if (showFabMenu) {
            showFabMenu = false
        } else {
            selectedTab = 0
        }
    }

    BolaoScaffold(
        containerColor = DeepNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            MainFabMenu(
                showMenu = showFabMenu,
                onToggleMenu = { showFabMenu = !showFabMenu },
                onCreateBolao = onNavigateToCreateBolao,
                onJoinBolao = onNavigateToJoinBolao
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 ->
                    HomeScreen(
                        onNavigateToBolao = onNavigateToBolao,
                        onNavigateToCreateBolao = onNavigateToCreateBolao,
                        onNavigateToJoinBolao = onNavigateToJoinBolao,
                        onNavigateToAccount = { selectedTab = 1 }
                    )
                1 ->
                    ProfileScreen(
                        onNavigateToHelp = onNavigateToHelp,
                        onNavigateBack = { selectedTab = 0 },
                        onSignOut = onSignOut
                    )
            }
        }
    }
}

@Composable
private fun MainFabSubItems(onToggleMenu: () -> Unit, onCreateBolao: () -> Unit, onJoinBolao: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
        FabSubItem(
            icon = Icons.Default.Search,
            label = stringResource(Res.string.main_screen_fab_join_with_code),
            onClick = {
                onToggleMenu()
                onJoinBolao()
            }
        )
        FabSubItem(
            icon = Icons.Default.Add,
            label = stringResource(Res.string.main_screen_fab_create_bolao),
            onClick = {
                onToggleMenu()
                onCreateBolao()
            }
        )
        Spacer(Modifier.height(8.dp)) // Reduced internal spacing
    }
}

@Composable
private fun BouncingSoccerBallIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_ball")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "fab_ball_rotation"
    )
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(450, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "fab_ball_bounce"
    )

    BolaoText(
        text = stringResource(Res.string.main_screen_fab_ball_emoji),
        fontSize = BolaoTypography.displayMedium.fontSize,
        // translationY here (draw phase) instead of Modifier.offset (layout phase) so the
        // continuous bounce never triggers a relayout, just a cheap redraw of this layer.
        modifier =
        Modifier.graphicsLayer {
            translationY = bounce.dp.toPx()
            rotationZ = rotation
        }
    )
}

@Composable
private fun MainFabButton(onToggleMenu: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
        Modifier.size(56.dp).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onToggleMenu
        ),
        contentAlignment = Alignment.Center
    ) {
        BouncingSoccerBallIcon()
    }
}

@Composable
private fun MainFabMenu(showMenu: Boolean, onToggleMenu: () -> Unit, onCreateBolao: () -> Unit, onJoinBolao: () -> Unit) {
    Column(
        modifier = Modifier.padding(bottom = BolaoSpacing.xxxl + BolaoSpacing.xxl),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            MainFabSubItems(onToggleMenu, onCreateBolao, onJoinBolao)
        }
        MainFabButton(onToggleMenu)
    }
}

@Composable
private fun FabSubItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
        modifier =
        Modifier
            .clip(BolaoRadiusShape.md)
            .background(NavyElevated)
            .border(1.dp, GlassBorder, BolaoRadiusShape.md)
            .clickable(onClick = onClick)
            .padding(horizontal = BolaoSpacing.lg, vertical = BolaoSpacing.md)
    ) {
        BolaoIcon(icon, null, modifier = Modifier.size(18.dp), tint = Neon)
        BolaoText(label, fontSize = BolaoTypography.bodyLarge.fontSize, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    BolaoSurface(
        color = NavyCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                icon = Icons.Default.Home,
                label = stringResource(Res.string.main_screen_tab_boloes),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            // Larger gap for the central FAB, evenly pushing the menus away from the center
            Spacer(Modifier.width(140.dp))

            TabItem(
                icon = Icons.Default.Person,
                label = stringResource(Res.string.main_screen_tab_conta),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
private fun TabItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(if (isSelected) Neon else TextMuted)

    Column(
        modifier =
        Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(BolaoSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BolaoIcon(icon, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
        BolaoText(
            text = label,
            color = color,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
