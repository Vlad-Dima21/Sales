package vlad.dima.sales.ui.enter_account

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.salesman_dashboard.SalesmanDashboardActivity
import vlad.dima.sales.ui.theme.*


var errorMessage by mutableStateOf("")

class EnterAccountActivity : ComponentActivity() {

    private lateinit var viewModel: EnterAccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDAO())

        viewModel = ViewModelProvider(this, EnterAccountViewModel.Factory(repository))[EnterAccountViewModel::class.java]

        if (viewModel.isLoggedIn()) {
            startActivity(Intent(this, SalesmanDashboardActivity::class.java))
            finish()
        }

        // set the app to be in fullscreen (can draw behind status bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // the activity observes the results of viewModel operations
        viewModel.actionResult.observe(this) { result ->
            if (!result.actionSuccessful) {
                    errorMessage = getString(result.messageStringId)
            } else {
                Toast.makeText(this, result.messageStringId, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SalesmanDashboardActivity::class.java))
                finish()
            }
        }

        setContent {
            // force portrait mode in current activity
            (LocalContext.current as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                }

                Box(
                    modifier = backgroundModifier
                        .fillMaxSize()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(.9f)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
                        elevation = 5.dp,
                        backgroundColor = if (isSystemInDarkTheme()) DarkBackground else LightSurface,
                        border = if (isSystemInDarkTheme()) BorderStroke(2.dp, DarkSurface) else BorderStroke(0.dp, LightSurface)
                    ) {
                        EnterAccountNavigation(viewModel)
                    }

                    if (errorMessage.isNotEmpty()) {
                        ErrorPopup(
                            message = errorMessage,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 100.dp))
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