package com.example.mustase.prescription.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mustase.prescription.ui.screen.DetailScreen
import com.example.mustase.prescription.ui.screen.HistoryScreen
import com.example.mustase.prescription.ui.screen.ScanScreen

sealed class Screen(val route: String) {
    data object History : Screen("history")
    data object Scan : Screen("scan")
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Long) = "detail/$id"
    }
}

@Composable
fun PrescriptionNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.History.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Écran d'historique (accueil)
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToScan = {
                    navController.navigate(Screen.Scan.route)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
        }

        // Écran de scan
        composable(Screen.Scan.route) {
            ScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onScanSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Écran de détail
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val prescriptionId = backStackEntry.arguments?.getLong("id") ?: 0L
            DetailScreen(
                prescriptionId = prescriptionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

