package com.lpstudio.bolaodagalera.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.main_screen_fab_create_bolao
import bolaodagalera.composeapp.generated.resources.main_screen_fab_join_with_code
import bolaodagalera.composeapp.generated.resources.main_screen_tab_boloes
import bolaodagalera.composeapp.generated.resources.main_screen_tab_conta
import bolaodagalera.composeapp.generated.resources.main_screen_tab_criar_bolao
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
    var showCreateMenu by remember { mutableStateOf(false) }

    // Handle system back button to return to Home from Account
    CommonBackHandler(enabled = selectedTab != 0 || showCreateMenu) {
        if (showCreateMenu) {
            showCreateMenu = false
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
                onTabSelected = { selectedTab = it },
                showCreateMenu = showCreateMenu,
                onToggleCreateMenu = { showCreateMenu = !showCreateMenu }
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

            CreateBolaoMenuOverlay(
                visible = showCreateMenu,
                onCreateBolao = {
                    showCreateMenu = false
                    onNavigateToCreateBolao()
                },
                onJoinBolao = {
                    showCreateMenu = false
                    onNavigateToJoinBolao()
                }
            )
        }
    }
}

/** Anchored above the bottom nav's central "Criar bolão" item, inside the content area it insets into. */
@Composable
private fun CreateBolaoMenuOverlay(visible: Boolean, onCreateBolao: () -> Unit, onJoinBolao: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            CreateBolaoMenuOptions(onCreateBolao = onCreateBolao, onJoinBolao = onJoinBolao)
        }
    }
}

@Composable
private fun CreateBolaoMenuOptions(onCreateBolao: () -> Unit, onJoinBolao: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.xxl, vertical = BolaoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)
    ) {
        CreateMenuOption(
            icon = Icons.Default.Add,
            label = stringResource(Res.string.main_screen_fab_create_bolao),
            onClick = onCreateBolao
        )
        CreateMenuOption(
            icon = Icons.Default.Search,
            label = stringResource(Res.string.main_screen_fab_join_with_code),
            onClick = onJoinBolao
        )
    }
}

@Composable
private fun CreateMenuOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
        modifier =
        Modifier
            .fillMaxWidth()
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
private fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit, showCreateMenu: Boolean, onToggleCreateMenu: () -> Unit) {
    BolaoSurface(
        color = NavyCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                icon = Icons.Default.Home,
                label = stringResource(Res.string.main_screen_tab_boloes),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            TabItem(
                icon = Icons.Default.Add,
                label = stringResource(Res.string.main_screen_tab_criar_bolao),
                isSelected = showCreateMenu,
                onClick = onToggleCreateMenu
            )

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
