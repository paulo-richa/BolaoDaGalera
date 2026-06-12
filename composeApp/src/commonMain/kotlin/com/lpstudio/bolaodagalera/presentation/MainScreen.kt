package com.lpstudio.bolaodagalera.presentation

import com.lpstudio.bolaodagalera.CommonBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.presentation.auth.ProfileScreen
import com.lpstudio.bolaodagalera.presentation.home.HomeScreen
import com.lpstudio.bolaodagalera.presentation.theme.*

@Composable
fun MainScreen(
    onNavigateToBolao: (String) -> Unit,
    onNavigateToCreateBolao: () -> Unit,
    onNavigateToJoinBolao: () -> Unit,
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

    Scaffold(
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
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(modifier = Modifier.padding(bottom = 0.dp)) { // Padding manual para evitar cortes
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigateToBolao = onNavigateToBolao,
                    onNavigateToCreateBolao = onNavigateToCreateBolao,
                    onNavigateToJoinBolao = onNavigateToJoinBolao,
                    onNavigateToAccount = { selectedTab = 1 }
                )
                1 -> ProfileScreen(
                    onNavigateBack = { selectedTab = 0 },
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
private fun MainFabMenu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    onCreateBolao: () -> Unit,
    onJoinBolao: () -> Unit
) {
    Column(
        modifier = Modifier.offset(y = 52.dp), // Desce o círculo para a linha da barra
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabSubItem(
                    icon = Icons.Default.Search,
                    label = "Entrar com código",
                    onClick = { onToggleMenu(); onJoinBolao() }
                )
                FabSubItem(
                    icon = Icons.Default.Add,
                    label = "Criar novo bolão",
                    onClick = { onToggleMenu(); onCreateBolao() }
                )
                Spacer(Modifier.height(8.dp)) // Espaçamento interno reduzido
            }
        }

        // Main FAB
        val rotation by animateFloatAsState(
            targetValue = if (showMenu) 45f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "fab_rotation"
        )

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GradientPrimary)
                .clickable(onClick = onToggleMenu),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = DeepNavy,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabSubItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavyElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Neon)
        Text(label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = NavyCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                icon = Icons.Default.Home,
                label = "Bolões",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            // Espaço maior para o FAB central, afastando os menus do centro de forma equilibrada
            Spacer(Modifier.width(140.dp))

            TabItem(
                icon = Icons.Default.Person,
                label = "Conta",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
private fun TabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color by animateColorAsState(if (isSelected) Neon else TextMuted)
    
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
        Text(
            text = label,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
