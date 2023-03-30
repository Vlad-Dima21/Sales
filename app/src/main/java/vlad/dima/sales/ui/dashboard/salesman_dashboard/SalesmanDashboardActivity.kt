package vlad.dima.sales.ui.dashboard.salesman_dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.SalesmanClientsPage
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.SalesmanPastSales
import vlad.dima.sales.ui.dashboard.SalesmanDashboardResources
import vlad.dima.sales.ui.theme.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.SalesmanClientsViewModel
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.new_order.NewOrderActivity
import vlad.dima.sales.ui.dashboard.salesman_dashboard.notifications.SalesmanNotificationsViewModel
import vlad.dima.sales.ui.dashboard.salesman_dashboard.past_sales.SalesmanPastSalesViewModel
import vlad.dima.sales.ui.enter_account.EnterAccountActivity

class SalesmanDashboardActivity : ComponentActivity() {

    private lateinit var notificationResultActivity: ActivityResultLauncher<Intent>
    private lateinit var clientResultActivity: ActivityResultLauncher<Intent>

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())

        val notificationsViewModel: SalesmanNotificationsViewModel = ViewModelProvider(
            owner = this,
            factory = SalesmanNotificationsViewModel.Factory(repository)
        )[SalesmanNotificationsViewModel::class.java]
        val pastSalesViewModel: SalesmanPastSalesViewModel = ViewModelProvider(this)[SalesmanPastSalesViewModel::class.java]
        val clientsViewModel: SalesmanClientsViewModel = ViewModelProvider(this)[SalesmanClientsViewModel::class.java]

        val currentUserLD = repository.getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid)

        // update current user across viewModels
        currentUserLD.observe(this) {user ->
            if (user != null) {
                notificationsViewModel.currentUserSate = user
                notificationsViewModel.loadItems()

                clientsViewModel.currentUserState = user
                clientsViewModel.loadClients()
            }
        }

        var navController: NavHostController? = null

        notificationResultActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data?.getBooleanExtra("isNotificationDeleted", false) == true) {
                    notificationsViewModel.loadItems()
                }
            }
        }

        clientResultActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                navController?.navigate(SalesmanDashboardResources.PastSales.route)
                var client: Client
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    client = result.data?.getSerializableExtra("client", Client::class.java)!!
                } else {
                    client = result.data?.getSerializableExtra("client") as Client
                }
                Toast.makeText(this, client.clientName, Toast.LENGTH_SHORT).show()
            }
        }

        notificationsInitialize(notificationsViewModel)
        clientsInitialize(clientsViewModel)

        setContent {
            SalesTheme {
                navController = rememberAnimatedNavController()

                Scaffold(
                    bottomBar = {
                        SalesmanDashboardBottomNavigation(navController = navController!!)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SalesmanDashboardNavigation(
                            navController = navController!!,
                            notificationsViewModel = notificationsViewModel,
                            pastSalesViewModel = pastSalesViewModel,
                            clientsViewModel = clientsViewModel
                        )
                    }
                }
            }
        }
    }

    private fun clientsInitialize(clientsViewModel: SalesmanClientsViewModel) = lifecycleScope.launch {
        clientsViewModel.isCreatingOrderIntent.collect { client ->
            if (client != null) {
                clientResultActivity.launch(
                    Intent(this@SalesmanDashboardActivity, NewOrderActivity::class.java)
                        .putExtra("client", client)
                )
            }
        }
    }

    private fun notificationsInitialize(notificationsViewModel: SalesmanNotificationsViewModel) {
        // logout functionality
        notificationsViewModel.isUserLoggedIn.observe(this) { isUserLoggedIn ->
            if (!isUserLoggedIn) {
                startActivity(Intent(this, EnterAccountActivity::class.java))
                finish()
            }
        }

        lifecycleScope.launch {
            notificationsViewModel.isViewingNotificationIntent.collect {
                if (it != null) {
                    notificationResultActivity.launch(it)
                    notificationsViewModel.isViewingNotificationIntent.emit(null)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SalesmanDashboardNavigation(
    navController: NavController,
    notificationsViewModel: SalesmanNotificationsViewModel,
    pastSalesViewModel: SalesmanPastSalesViewModel,
    clientsViewModel: SalesmanClientsViewModel
) {
    val context = LocalContext.current

    AnimatedNavHost(
        navController = navController as NavHostController,
        startDestination = SalesmanDashboardResources.Notifications.route,
        enterTransition = {
            fadeIn(initialAlpha = 1f)
        },
        exitTransition = {
            fadeOut(animationSpec = tween(0))
        }
    ) {
        composable(route = SalesmanDashboardResources.Notifications.route) {
            SalesmanNotificationsPage(notificationsViewModel)
        }

        composable(route = SalesmanDashboardResources.PastSales.route) {
            SalesmanPastSales(pastSalesViewModel)
        }

        composable(route = SalesmanDashboardResources.Clients.route) {
            SalesmanClientsPage(clientsViewModel)
        }
    }
}

@Composable
fun SalesmanDashboardBottomNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedPage = listOf(SalesmanDashboardResources.Notifications, SalesmanDashboardResources.PastSales, SalesmanDashboardResources.Clients)
        .find { p -> p.route == backStackEntry.value?.destination?.route }

    BottomNavigation(
        modifier = Modifier.height(80.dp),
        backgroundColor = MaterialTheme.colors.background,
        elevation = dimensionResource(id = R.dimen.standard_elevation)
    ) {
        BottomNavigationItem(
            selected = SalesmanDashboardResources.Notifications == selectedPage,
            onClick = {
                val previousRoute = selectedPage?.route
                navController.navigate(SalesmanDashboardResources.Notifications.route) {
                    if (previousRoute != null) popUpTo(previousRoute) {inclusive = true}
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (SalesmanDashboardResources.Notifications == selectedPage) {
                        true -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.Notifications.iconSelected,
                                contentDescription = context.getString(SalesmanDashboardResources.Notifications.title)
                            )
                            Text(
                                text = context.getString(SalesmanDashboardResources.Notifications.title),
                                color = GreenPrimary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.Notifications.icon,
                                contentDescription = context.getString(SalesmanDashboardResources.Notifications.title)
                            )
                        }
                    }
                }
            },
            selectedContentColor = GreenPrimary,
            unselectedContentColor = Color.Gray
        )
        BottomNavigationItem(
            selected = SalesmanDashboardResources.PastSales == selectedPage,
            onClick = {
                val previousRoute = selectedPage?.route
                navController.navigate(SalesmanDashboardResources.PastSales.route) {
                    if (previousRoute != null) popUpTo(previousRoute) {inclusive = true}
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (SalesmanDashboardResources.PastSales == selectedPage) {
                        true -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.PastSales.iconSelected,
                                contentDescription = context.getString(SalesmanDashboardResources.PastSales.title)
                            )
                            Text(
                                text = context.getString(SalesmanDashboardResources.PastSales.title),
                                color = GreenPrimary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.PastSales.icon,
                                contentDescription = context.getString(SalesmanDashboardResources.PastSales.title)
                            )
                        }
                    }
                }
            },
            selectedContentColor = GreenPrimary,
            unselectedContentColor = Color.Gray
        )
        BottomNavigationItem(
            selected = SalesmanDashboardResources.Clients == selectedPage,
            onClick = {
                val previousRoute = selectedPage?.route
                navController.navigate(SalesmanDashboardResources.Clients.route) {
                    if (previousRoute != null) popUpTo(previousRoute) {inclusive = true}
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (SalesmanDashboardResources.Clients == selectedPage) {
                        true -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.Clients.iconSelected,
                                contentDescription = context.getString(SalesmanDashboardResources.Clients.title)
                            )
                            Text(
                                text = context.getString(SalesmanDashboardResources.Clients.title),
                                color = GreenPrimary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = SalesmanDashboardResources.Clients.icon,
                                contentDescription = context.getString(SalesmanDashboardResources.Clients.title)
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
