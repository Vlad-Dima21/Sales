package vlad.dima.sales.ui.dashboard.manager_dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.ManagerDashboardResources
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.ManagerNotificationsPage
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.ManagerNotificationsViewModel
import vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.new_notification.NewNotification
import vlad.dima.sales.ui.enter_account.EnterAccountActivity
import vlad.dima.sales.ui.theme.GreenPrimary
import vlad.dima.sales.ui.theme.SalesTheme

class ManagerDashboardActivity : ComponentActivity() {

    private lateinit var notificationsViewModel: ManagerNotificationsViewModel

    private lateinit var addedActivityResult: ActivityResultLauncher<Intent>
    private lateinit var deletedActivityResult: ActivityResultLauncher<Intent>

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())
        notificationsViewModel = ViewModelProvider(
            owner = this,
            factory = ManagerNotificationsViewModel.Factory(repository)
        )[ManagerNotificationsViewModel::class.java]

        addedActivityResult = registerAddedActivityResult()
        deletedActivityResult = registerDeletedActivityResult()

        notificationsInitialize(notificationsViewModel)

        setContent {
            SalesTheme {
                val navController = rememberAnimatedNavController()

                Scaffold(
                    bottomBar = {
                        ManagerDashboardBottomNavigation(navController = navController)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
//                        ManagerDashboardNavigation(
//                            navController = navController,
//                            notificationsViewModel = notificationsViewModel
//                        )
                        ManagerNotificationsPage(viewModel = notificationsViewModel)
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

        // update current user
        notificationsViewModel.currentUserLD.observe(this) {user ->
            if (user != null) {
                notificationsViewModel.currentUserSate = user
                notificationsViewModel.loadItems()
            }
        }

        lifecycleScope.launch {
            notificationsViewModel.isCreatingNewNotification.collect {
                if (it) {
                    addedActivityResult.launch(
                        Intent(this@ManagerDashboardActivity, NewNotification::class.java)
                    )
                    notificationsViewModel.isCreatingNewNotification.emit(false)
                }
            }
        }

        lifecycleScope.launch {
            notificationsViewModel.isViewingNotification.collect {
                if (it != null) {
                    deletedActivityResult.launch(it)
                    notificationsViewModel.isViewingNotification.emit(null)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ManagerDashboardNavigation(
    navController: NavHostController,
    notificationsViewModel: ManagerNotificationsViewModel
) {
    val context = LocalContext.current

    AnimatedNavHost(
        navController = navController,
        startDestination = ManagerDashboardResources.Notifications.route,
        enterTransition = {
            fadeIn(initialAlpha = 1f)
        },
        exitTransition = {
            fadeOut(animationSpec = tween(0))
        }
    ) {
        composable(route = ManagerDashboardResources.Notifications.route) {
            ManagerNotificationsPage(notificationsViewModel)
        }
    }
}

@Composable
fun ManagerDashboardBottomNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedPage = ManagerDashboardResources.Notifications

    BottomNavigation(
        modifier = Modifier.height(80.dp),
        backgroundColor = MaterialTheme.colors.background,
        elevation = dimensionResource(id = R.dimen.standard_elevation)
    ) {
        BottomNavigationItem(
            selected = ManagerDashboardResources.Notifications == selectedPage,
            onClick = {
//                val previousRoute = selectedPage.route
//                navController.navigate(ManagerDashboardResources.Notifications.route) {
//                    popUpTo(previousRoute) { inclusive = true }
//                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (ManagerDashboardResources.Notifications == selectedPage) {
                        true -> {
                            Icon(
                                imageVector = ManagerDashboardResources.Notifications.iconSelected,
                                contentDescription = context.getString(ManagerDashboardResources.Notifications.title)
                            )
                            Text(
                                text = context.getString(ManagerDashboardResources.Notifications.title),
                                color = GreenPrimary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = ManagerDashboardResources.Notifications.icon,
                                contentDescription = context.getString(ManagerDashboardResources.Notifications.title)
                            )
                        }
                    }
                }
            },
            selectedContentColor = GreenPrimary,
            unselectedContentColor = Color.Gray
        )
    }
}
