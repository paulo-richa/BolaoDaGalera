package com.lpstudio.bolaodagalera.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigation.navDeepLink
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.MainScreen
import com.lpstudio.bolaodagalera.presentation.auth.LoginScreen
import com.lpstudio.bolaodagalera.presentation.auth.ProfileScreen
import com.lpstudio.bolaodagalera.presentation.auth.RegisterScreen
import com.lpstudio.bolaodagalera.presentation.bolao.AddParticipantsScreen
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoDetailScreen
import com.lpstudio.bolaodagalera.presentation.bolao.EditBolaoScreen
import com.lpstudio.bolaodagalera.presentation.bolao.CreateBolaoScreen
import com.lpstudio.bolaodagalera.presentation.bolao.JoinBolaoScreen
import com.lpstudio.bolaodagalera.presentation.home.HomeScreen
import com.lpstudio.bolaodagalera.presentation.match.PredictionScreen
import org.koin.compose.koinInject

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinInject()
    val authUiState by authViewModel.uiState.collectAsState()

    if (!authUiState.isAuthChecked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Definimos a rota inicial apenas uma vez quando o estado de auth é verificado.
    // Isso evita que o NavHost reinicie do zero ao fazer logout.
    val startDestination = remember { if (authUiState.user != null) Home else Login }

    LaunchedEffect(authUiState.user) {
        if (authUiState.user == null && authUiState.isAuthChecked) {
            // Logout detectado: Limpa toda a pilha e volta para o Login de forma estável.
            navController.navigate(Login) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Register) }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Home> {
            MainScreen(
                onNavigateToBolao = { bolaoId -> navController.navigate(BolaoDetail(bolaoId)) },
                onNavigateToCreateBolao = { navController.navigate(CreateBolao) },
                onNavigateToJoinBolao = { navController.navigate(JoinBolao) },
                onSignOut = { /* O LaunchedEffect acima cuidará do logout global */ }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = { /* O LaunchedEffect acima cuidará do logout global */ }
            )
        }

        composable<CreateBolao> {
            CreateBolaoScreen(
                onCreated = { bolaoId ->
                    navController.navigate(BolaoDetail(bolaoId)) {
                        popUpTo(Home)
                    }
                },
                onNavigateToAddParticipants = { bolaoId ->
                    navController.navigate(AddParticipants(bolaoId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<JoinBolao>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://bolaodagalera.app/invite?code={code}" },
                navDeepLink { uriPattern = "bolaodagalera://invite?code={code}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<JoinBolao>()
            // Tenta pegar o código do deep link (desativado para build KMP iOS estável por enquanto)
            val codeFromDeepLink = ""
            
            JoinBolaoScreen(
                initialCode = codeFromDeepLink,
                onJoined = { bolaoId ->
                    navController.navigate(BolaoDetail(bolaoId)) {
                        popUpTo(Home)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AddParticipants> { backStackEntry ->
            val route = backStackEntry.toRoute<AddParticipants>()
            AddParticipantsScreen(
                bolaoId = route.bolaoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<BolaoDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<BolaoDetail>()
            BolaoDetailScreen(
                bolaoId = route.bolaoId,
                onNavigateToPrediction = { matchId ->
                    navController.navigate(Prediction(bolaoId = route.bolaoId, matchId = matchId))
                },
                onNavigateToAllPredictions = { matchId ->
                    navController.navigate(MatchPredictions(bolaoId = route.bolaoId, matchId = matchId))
                },
                onNavigateToEdit = { bolaoId ->
                    navController.navigate(EditBolao(bolaoId))
                },
                onNavigateToAddParticipants = { bolaoId ->
                    navController.navigate(AddParticipants(bolaoId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<EditBolao> { backStackEntry ->
            val route = backStackEntry.toRoute<EditBolao>()
            EditBolaoScreen(
                bolaoId = route.bolaoId,
                onNavigateToAddParticipants = { bolaoId ->
                    navController.navigate(AddParticipants(bolaoId))
                },
                onBolaoDeleted = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Prediction> { backStackEntry ->
            val route = backStackEntry.toRoute<Prediction>()
            PredictionScreen(
                bolaoId = route.bolaoId,
                matchId = route.matchId,
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<MatchPredictions> { backStackEntry ->
            val route = backStackEntry.toRoute<MatchPredictions>()
            com.lpstudio.bolaodagalera.presentation.match.MatchPredictionsScreen(
                bolaoId = route.bolaoId,
                matchId = route.matchId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
