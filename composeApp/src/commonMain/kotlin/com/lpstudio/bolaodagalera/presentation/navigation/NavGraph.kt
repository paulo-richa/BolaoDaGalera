package com.lpstudio.bolaodagalera.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.lpstudio.bolaodagalera.presentation.MainScreen
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.auth.LoginScreen
import com.lpstudio.bolaodagalera.presentation.auth.ProfileScreen
import com.lpstudio.bolaodagalera.presentation.auth.RegisterScreen
import com.lpstudio.bolaodagalera.presentation.bolao.AddParticipantsScreen
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoDetailScreen
import com.lpstudio.bolaodagalera.presentation.bolao.CreateBolaoScreen
import com.lpstudio.bolaodagalera.presentation.bolao.EditBolaoScreen
import com.lpstudio.bolaodagalera.presentation.bolao.JoinBolaoScreen
import com.lpstudio.bolaodagalera.presentation.help.HelpScreen
import com.lpstudio.bolaodagalera.presentation.match.PredictionScreen
import com.lpstudio.bolaodagalera.util.TimeSource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    if (!authUiState.isAuthChecked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = remember { if (authUiState.user != null) Home else Login }

    var lastBackgroundTime by remember { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    lastBackgroundTime = TimeSource.nowMillis()
                } else if (event == Lifecycle.Event.ON_RESUME) {
                    lastBackgroundTime?.let { bgTime ->
                        val now = TimeSource.nowMillis()
                        val diffMillis = now - bgTime
                        if (diffMillis >= 600_000 && authUiState.user != null) {
                            navController.navigate(Home) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    lastBackgroundTime = null
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(authUiState.user) {
        if (authUiState.user == null && authUiState.isAuthChecked) {
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
                onNavigateToRegister = { email -> navController.navigate(Register(email)) }
            )
        }

        composable<Register> { backStackEntry ->
            val route = backStackEntry.toRoute<Register>()
            RegisterScreen(
                initialEmail = route.email ?: "",
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
                onNavigateToJoinBolao = { navController.navigate(JoinBolao()) },
                onNavigateToHelp = { navController.navigate(Help) },
                onSignOut = { }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onNavigateToHelp = { navController.navigate(Help) },
                onNavigateBack = { navController.popBackStack() },
                onSignOut = { }
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
            deepLinks =
            listOf(
                navDeepLink<JoinBolao>(basePath = "https://bolaodagalera.app/invite"),
                navDeepLink<JoinBolao>(basePath = "http://bolaodagalera.app/invite"),
                navDeepLink<JoinBolao>(basePath = "https://www.bolaodagalera.app/invite"),
                navDeepLink<JoinBolao>(basePath = "https://bolaodagalera-bb002.web.app/invite"),
                navDeepLink<JoinBolao>(basePath = "http://bolaodagalera-bb002.web.app/invite"),
                navDeepLink { uriPattern = "bolaodagalera://invite?code={code}" }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<JoinBolao>()
            val codeFromDeepLink = route.code ?: ""

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

        composable<BolaoDetail>(
            deepLinks = listOf(navDeepLink { uriPattern = "bolaodagalera://bolao?bolaoId={bolaoId}" })
        ) { backStackEntry ->
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
                onNavigateToHelp = { navController.navigate(Help) },
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

        composable<Prediction>(
            deepLinks = listOf(navDeepLink { uriPattern = "bolaodagalera://predict?bolaoId={bolaoId}&matchId={matchId}" })
        ) { backStackEntry ->
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

        composable<Help> {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
