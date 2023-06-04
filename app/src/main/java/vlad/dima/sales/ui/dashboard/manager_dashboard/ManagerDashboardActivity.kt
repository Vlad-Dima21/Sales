package vlad.dima.sales.ui.dashboard.manager_dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.dashboard.ManagerDashboardResources
import vlad.dima.sales.ui.dashboard.common.AnimatedBottomNavigationItem
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.ManagerNotificationsPage
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.ManagerNotificationsViewModel
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.new_notification.NewNotification
import vlad.dima.sales.ui.dashboard.manager_dashboard.productsStats.ProductsStatsPage
import vlad.dima.sales.ui.dashboard.manager_dashboard.productsStats.ProductsStatsViewModel
import vlad.dima.sales.ui.enter_account.EnterAccountActivity
import vlad.dima.sales.ui.theme.SalesTheme

class ManagerDashboardActivity : ComponentActivity() {

    private lateinit var notificationsViewModel: ManagerNotificationsViewModel
    private lateinit var productsStatsViewModel: ProductsStatsViewModel

    private lateinit var addedActivityResult: ActivityResultLauncher<Intent>
    private lateinit var deletedActivityResult: ActivityResultLauncher<Intent>

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())
        val networkManager = NetworkManager(applicationContext)
        notificationsViewModel = ViewModelProvider(
            owner = this,
            factory = ManagerNotificationsViewModel.Factory(repository, networkManager)
        )[ManagerNotificationsViewModel::class.java]

        productsStatsViewModel = ViewModelProvider(
            owner = this,
            factory = ProductsStatsViewModel.Factory(networkManager)
        )[ProductsStatsViewModel::class.java]

        addedActivityResult = registerAddedActivityResult()
        deletedActivityResult = registerDeletedActivityResult()

        var navController: NavHostController?

        notificationsInitialize(notificationsViewModel)

        setContent {
            SalesTheme {
                navController = rememberAnimatedNavController()
                val networkStatus by networkManager.currentConnection.collectAsState(NetworkManager.NetworkStatus.Available)
                val uiController = rememberSystemUiController()
                val systemBarsColor by animateColorAsState(
                    if (networkStatus != NetworkManager.NetworkStatus.Available) MaterialTheme.colors.error else MaterialTheme.colors.primary,
                    label = "statusBarColor"
                )
                uiController.setStatusBarColor(systemBarsColor)
                uiController.setNavigationBarColor(MaterialTheme.colors.background)
                Scaffold(
                    bottomBar = {
                        ManagerDashboardBottomNavigation(navController = navController!!)
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        AnimatedVisibility(visible = networkStatus != NetworkManager.NetworkStatus.Available) {
                            Text(
                                text = stringResource(id = R.string.CheckConnection),
                                color = MaterialTheme.colors.onError,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(systemBarsColor)
                                    .padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        ManagerDashboardNavigation(
                            navController = navController!!,
                            notificationsViewModel = notificationsViewModel,
                            productsStatsViewModel = productsStatsViewModel
                        )
                    }
                }
            }
        }
    }

    private fun registerAddedActivityResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val isNotificationAdded = result.data?.getBooleanExtra("isNotificationAdded", false)
                if (isNotificationAdded == true) {
                    notificationsViewModel.loadItems()
                }
            }
        }
    }

    private fun registerDeletedActivityResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data?.getBooleanExtra("isNotificationDeleted", false) == true) {
                    notificationsViewModel.loadItems()
                }
            }
        }
    }

    private fun notificationsInitialize(notificationsViewModel: ManagerNotificationsViewModel) {
        // logout functionality
        notificationsViewModel.isUserLoggedIn.observe(this) { isUserLoggedIn ->
            if (!isUserLoggedIn) {
                startActivity(Intent(this, EnterAccountActivity::class.java))
                finish()
            }
        }

        lifecycleScope.launch {
            notificationsViewModel.isCreatingNewNotification.collect {
                if (it) {
                    addedActivityResult.launch(
                        Intent(this@ManagerDashboardActivity, NewNotification::class.java)
                    )
                }
            }
        }

        lifecycleScope.launch {
            notificationsViewModel.isViewingNotification.collect {
                if (it != null) {
                    deletedActivityResult.launch(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ManagerDashboardNavigation(
    navController: NavController,
    notificationsViewModel: ManagerNotificationsViewModel,
    productsStatsViewModel: ProductsStatsViewModel
) {
    AnimatedNavHost(
        navController = navController as NavHostController,
        startDestination = ManagerDashboardResources.Notifications.route,
        enterTransition = {
            fadeIn(initialAlpha = 1f)
        },
        exitTransition = {
            fadeOut(animationSpec = tween(0))
        }
    ) {
        composable(route = ManagerDashboardResources.Notifications.route) {
            ManagerNotificationsPage(viewModel = notificationsViewModel)
        }
        composable(route = ManagerDashboardResources.ProductsStats.route) {
            ProductsStatsPage(viewModel = productsStatsViewModel)
        }
    }
}

@Composable
fun ManagerDashboardBottomNavigation(navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedPage = listOf(
        ManagerDashboardResources.Notifications,
        ManagerDashboardResources.ProductsStats
    )
        .find { it.route == backStackEntry.value?.destination?.route }

    BottomNavigation(
        modifier = Modifier.height(80.dp),
        backgroundColor = MaterialTheme.colors.background,
        elevation = dimensionResource(id = R.dimen.standard_elevation)
    ) {
        AnimatedBottomNavigationItem(
            isSelected = ManagerDashboardResources.Notifications == selectedPage,
            onClick = {
                val previousRoute = selectedPage?.route
                navController.navigate(ManagerDashboardResources.Notifications.route) {
                    if(previousRoute != null) popUpTo(previousRoute) { inclusive = true; saveState = true }
                    restoreState = true
                }
            },
            resource = ManagerDashboardResources.Notifications
        )
        AnimatedBottomNavigationItem(
            isSelected = ManagerDashboardResources.ProductsStats == selectedPage,
            onClick = {
                val previousRoute = selectedPage?.route
                navController.navigate(ManagerDashboardResources.ProductsStats.route) {
                    if(previousRoute != null) popUpTo(previousRoute) { inclusive = true; saveState = true }
                    restoreState = true
                }
            },
            resource = ManagerDashboardResources.ProductsStats
        )
    }
}
