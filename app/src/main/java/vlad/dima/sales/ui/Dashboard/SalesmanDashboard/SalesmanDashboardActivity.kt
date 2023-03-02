package vlad.dima.sales.ui.Dashboard.SalesmanDashboard

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.firebase.auth.FirebaseAuth
import vlad.dima.sales.ui.Dashboard.SalesmanDashboardResources
import vlad.dima.sales.ui.EnterAccount.EnterAccountActivity
import vlad.dima.sales.ui.theme.*

class SalesmanDashboardActivity : ComponentActivity() {

    private lateinit var viewmodel: SalesmanDashboardViewmodel

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            (LocalContext.current as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            SalesTheme {
                val navController = rememberAnimatedNavController()

                Scaffold(
                    bottomBar = {
                        DashboardBottomNavigation(navController = navController)
                    }
                ) {
                    DashboardNavigation(navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardNavigation(navController: NavController) {
    val context = LocalContext.current

    AnimatedNavHost(
        navController = navController as NavHostController,
        startDestination = SalesmanDashboardResources.Notifications.route
    ) {
        composable(route = SalesmanDashboardResources.Notifications.route) {
            NotificationsPage()
        }

        composable(route = SalesmanDashboardResources.PastSales.route) {
            PastSalesPage()
        }

        composable(route = SalesmanDashboardResources.Clients.route) {
            ClientsPage()
        }
    }
}

@Composable
fun DashboardBottomNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedPage = listOf(SalesmanDashboardResources.Notifications, SalesmanDashboardResources.PastSales, SalesmanDashboardResources.Clients)
        .find { p -> p.route == backStackEntry.value?.destination?.route }

    BottomNavigation(
        modifier = Modifier.height(80.dp),
        backgroundColor = if (isSystemInDarkTheme()) DarkSurface else LightSurface,
        elevation = 10.dp
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

@Composable
fun NotificationsPage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Notifications")
    }
}
@Composable
fun PastSalesPage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Past sales")
    }
}
@Composable
fun ClientsPage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Clients")
    }
}
