package vlad.dima.sales.ui.enter_account

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.manager_dashboard.ManagerDashboardActivity
import vlad.dima.sales.ui.dashboard.salesman_dashboard.SalesmanDashboardActivity
import vlad.dima.sales.ui.theme.*


var errorMessage by mutableStateOf("")

class EnterAccountActivity : ComponentActivity() {

    private lateinit var viewModel: EnterAccountViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())

        viewModel = ViewModelProvider(this, EnterAccountViewModel.Factory(repository))[EnterAccountViewModel::class.java]

        if (viewModel.isLoggedIn()) {
            repository.getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid).observe(this) {user ->
                if (user.managerUID == "") {
                    startActivity(Intent(this, ManagerDashboardActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, SalesmanDashboardActivity::class.java))
                    finish()
                }
            }
        }

        // set the app to be in fullscreen (can draw behind status bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // the activity observes the results of viewModel operations
        viewModel.actionResult.observe(this) { result ->
            if (!result.actionSuccessful) {
                    errorMessage = getString(result.messageStringId)
            } else {
                repository.getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid).observe(this) {user ->
                    if (user.managerUID == "") {
                        startActivity(Intent(this, ManagerDashboardActivity::class.java))
                        Toast.makeText(this, result.messageStringId, Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        startActivity(Intent(this, SalesmanDashboardActivity::class.java))
                        Toast.makeText(this, result.messageStringId, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        setContent {
            val backgroundModifier = when(isSystemInDarkTheme()) {
                true -> Modifier.background(Brush.linearGradient(
                    colors = listOf(DarkBackground, TealSecondaryDark)
                ))
                false -> Modifier.background(Brush.linearGradient(
                    colors = listOf(LightBackground, TealSecondaryLight)
                ))
            }
            SalesTheme {
                // set status bars colors to transparent
                window?.let {
                    it.statusBarColor = Color.Transparent.toArgb()
                    it.navigationBarColor = Color.Transparent.toArgb()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.isStatusBarContrastEnforced = MaterialTheme.colors.isLight
                    }
                }
                if (!viewModel.isLoggedIn()) {
                    Box(
                        modifier = backgroundModifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(.9f)
                                .align(Alignment.Center)
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
                            elevation = 5.dp,
                            backgroundColor = MaterialTheme.colors.background,
                            border = if (isSystemInDarkTheme()) BorderStroke(
                                2.dp,
                                DarkSurface
                            ) else BorderStroke(0.dp, LightSurface)
                        ) {
                            EnterAccountNavigation(viewModel)
                        }

                        if (errorMessage.isNotEmpty()) {
                            ErrorPopup(
                                message = errorMessage,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 100.dp)
                            )
                            LaunchedEffect(errorMessage) {
                                delay(2000)
                                errorMessage = ""
                            }
                        }
                    }
                }
            }
        }
    }
}