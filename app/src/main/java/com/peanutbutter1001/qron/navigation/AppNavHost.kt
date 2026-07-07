package com.peanutbutter1001.qron.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.peanutbutter1001.qron.feature.history.HistoryRoute
import com.peanutbutter1001.qron.feature.result.ResultRoute
import com.peanutbutter1001.qron.feature.scanner.ScannerRoute

/**
 * 앱의 단일 NavHost. 인앱 화면 전환은 전부 이곳의 destination으로 처리한다.
 * - 상단탭 destination: Scanner, History (하단 네비게이션 바 노출)
 * - 오버레이 destination: Result (바텀시트 형태, 하단 바 숨김)
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Result destination(바텀시트)에서는 하단 바를 숨긴다.
    val showBottomBar = currentDestination?.let {
        it.hasRoute(Scanner::class) || it.hasRoute(History::class)
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(Scanner::class) == true,
                        onClick = { navController.navigateToTopLevel(Scanner) },
                        icon = { Icons.Default.QrCode },
                        label = { Text("스캐너") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(History::class) == true,
                        onClick = { navController.navigateToTopLevel(History) },
                        icon = { Icons.Default.History },
                        label = { Text("기록") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Scanner,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Scanner> {
                ScannerRoute(
                    onNavigateToResult = { id -> navController.navigate(Result(id)) }
                )
            }
            composable<History> {
                HistoryRoute()
            }
            composable<Result> { entry ->
                val result = entry.toRoute<Result>()
                ResultRoute(
                    resultId = result.id,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}

/** 상단탭 간 이동 시 백스택을 관리하는 표준 패턴. */
private fun <T : Any> NavHostController.navigateToTopLevel(route: T) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
