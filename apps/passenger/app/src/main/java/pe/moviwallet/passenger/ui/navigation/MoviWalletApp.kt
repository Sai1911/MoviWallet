package pe.moviwallet.passenger.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.moviwallet.passenger.ui.screens.auth.LoginScreen
import pe.moviwallet.passenger.ui.screens.auth.RecoverScreen
import pe.moviwallet.passenger.ui.screens.auth.RegisterScreen
import pe.moviwallet.passenger.ui.screens.auth.WelcomeScreen
import pe.moviwallet.passenger.ui.screens.home.HomeScreen
import pe.moviwallet.passenger.ui.screens.operators.OperatorsScreen
import pe.moviwallet.passenger.ui.screens.pay.PayScreen
import pe.moviwallet.passenger.ui.screens.profile.ProfileScreen
import pe.moviwallet.passenger.ui.screens.topup.TopUpScreen
import pe.moviwallet.passenger.ui.screens.transfer.TransferScreen
import pe.moviwallet.passenger.ui.screens.wallet.ReceiptScreen
import pe.moviwallet.passenger.ui.screens.wallet.WalletScreen
import pe.moviwallet.passenger.ui.theme.AppBackground
import pe.moviwallet.passenger.ui.theme.Navy
import pe.moviwallet.passenger.viewmodel.AppViewModel

@Composable
fun MoviWalletApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()
    val startRoute = remember { if (state.user != null) Routes.HOME else Routes.WELCOME }
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route

    // Barra de estado acorde a cada pantalla (la de bienvenida es oscura).
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val dark = route == Routes.WELCOME
        window.statusBarColor = (if (dark) Navy else AppBackground).toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
    }

    val goHome: () -> Unit = {
        nav.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }
    val enterApp: () -> Unit = {
        nav.navigate(Routes.HOME) {
            popUpTo(Routes.WELCOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (route in Routes.TABS) {
                MoviBottomBar(
                    currentRoute = route,
                    onTab = { target -> nav.navigateToTab(target) },
                    onPay = { nav.navigate(Routes.PAY) { launchSingleTop = true } }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin = { nav.navigate(Routes.LOGIN) },
                    onRegister = { nav.navigate(Routes.REGISTER) },
                    onDemo = { vm.loginDemo(); enterApp() }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = { nav.popBackStack() },
                    onLogin = { email -> vm.login(email); enterApp() },
                    onDemo = { vm.loginDemo(); enterApp() },
                    onForgot = { nav.navigate(Routes.RECOVER) },
                    onRegister = { nav.navigate(Routes.REGISTER) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onBack = { nav.popBackStack() },
                    onRegister = { name, dni, email, profile ->
                        vm.register(name, dni, email, profile)
                        enterApp()
                    },
                    onLogin = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } } }
                )
            }
            composable(Routes.RECOVER) { RecoverScreen(onBack = { nav.popBackStack() }) }

            composable(Routes.HOME) {
                HomeScreen(
                    vm = vm,
                    onPay = { nav.navigate(Routes.PAY) { launchSingleTop = true } },
                    onTopUp = { nav.navigate(Routes.TOPUP) { launchSingleTop = true } },
                    onWallet = { nav.navigateToTab(Routes.WALLET) },
                    onOperators = { nav.navigateToTab(Routes.OPERATORS) },
                    onTransfer = { nav.navigate(Routes.TRANSFER) { launchSingleTop = true } },
                    onTransaction = { id -> nav.navigate(Routes.txDetail(id)) }
                )
            }
            composable(Routes.WALLET) {
                WalletScreen(
                    vm = vm,
                    onTopUp = { nav.navigate(Routes.TOPUP) { launchSingleTop = true } },
                    onTransaction = { id -> nav.navigate(Routes.txDetail(id)) }
                )
            }
            composable(Routes.OPERATORS) {
                OperatorsScreen(vm = vm, onTransfer = { nav.navigate(Routes.TRANSFER) { launchSingleTop = true } })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    vm = vm,
                    onLogout = {
                        vm.logout()
                        nav.navigate(Routes.WELCOME) {
                            popUpTo(nav.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.TOPUP) {
                TopUpScreen(
                    vm = vm,
                    onClose = { nav.popBackStack() },
                    onReceipt = { id -> nav.navigate(Routes.receipt(id)) }
                )
            }
            composable(Routes.PAY) {
                PayScreen(
                    vm = vm,
                    onClose = { nav.popBackStack() },
                    onHome = goHome,
                    onTopUp = { nav.navigate(Routes.TOPUP) { launchSingleTop = true } },
                    onReceipt = { id -> nav.navigate(Routes.receipt(id)) }
                )
            }
            composable(Routes.TRANSFER) {
                TransferScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(
                Routes.TX_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id")
                ReceiptScreen(
                    tx = state.transactions.firstOrNull { it.id == id },
                    title = "Detalle de la operación",
                    primaryLabel = "Volver",
                    onPrimary = { nav.popBackStack() },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Routes.RECEIPT,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id")
                ReceiptScreen(
                    tx = state.transactions.firstOrNull { it.id == id },
                    title = "Comprobante",
                    primaryLabel = "Listo",
                    onPrimary = goHome,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
