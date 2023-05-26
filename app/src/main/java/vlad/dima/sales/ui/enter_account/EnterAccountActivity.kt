package vlad.dima.sales.ui.enter_account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.manager_dashboard.ManagerDashboardActivity
import vlad.dima.sales.ui.dashboard.salesman_dashboard.SalesmanDashboardActivity
import vlad.dima.sales.ui.theme.*

val Context.dataStore by preferencesDataStore(name = "settings")

class EnterAccountActivity : ComponentActivity() {

    private lateinit var viewModel: EnterAccountViewModel

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())
        val networkManager = NetworkManager(applicationContext)

        viewModel = ViewModelProvider(
            this,
            EnterAccountViewModel.Factory(repository, networkManager)
        )[EnterAccountViewModel::class.java]

        if (viewModel.isLoggedIn()) {
            lifecycleScope.launch(Dispatchers.IO) {
                repository.getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collect { user ->
                        withContext(Dispatchers.Main) {
                            if (user.managerUID == "") {
                                startActivity(
                                    Intent(
                                        this@EnterAccountActivity,
                                        ManagerDashboardActivity::class.java
                                    )
                                )
                                finish()
                            } else {
                                startActivity(
                                    Intent(
                                        this@EnterAccountActivity,
                                        SalesmanDashboardActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }
            }
        }

        // set the app to be in fullscreen (can draw behind status bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // the activity observes the results of viewModel operations
        lifecycleScope.launch {
            viewModel.actionResult.collect { result ->
                if (result?.actionSuccessful == true) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        repository.getUserByUID(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collect { user ->
                                withContext(Dispatchers.Main) {
                                    if (user.managerUID == "") {
                                        startActivity(
                                            Intent(
                                                this@EnterAccountActivity,
                                                ManagerDashboardActivity::class.java
                                            )
                                        )
                                        finish()
                                    } else {
                                        startActivity(
                                            Intent(
                                                this@EnterAccountActivity,
                                                SalesmanDashboardActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                            }
                    }
                }
            }
        }

        setContent {
            SalesTheme(defaultSystemBarsColor = false) {
                val uiController = rememberSystemUiController()
                // set status bars colors to transparent
                with(uiController) {
                    setStatusBarColor(Color.Transparent, darkIcons = MaterialTheme.colors.isLight)
                    setNavigationBarColor(MaterialTheme.colors.secondary)
                }
                val backgroundModifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colors.background, MaterialTheme.colors.secondary)
                        )
                )
                val networkStatus by networkManager.currentConnection.collectAsState()
                val actionResult by viewModel.actionResult.collectAsState()
                if (!viewModel.isLoggedIn()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        AnimatedVisibility(visible = networkStatus != NetworkManager.NetworkStatus.Available) {
                            Text(
                                text = stringResource(id = R.string.CheckConnection),
                                color = MaterialTheme.colors.onError,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colors.error)
                                    .statusBarsPadding()
                                    .padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Box(
                            modifier = backgroundModifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(.9f)
                                    .align(Alignment.Center)
                                    .padding(vertical = 16.dp)
                                    .imePadding(),
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
                            androidx.compose.animation.AnimatedVisibility(
                                visible = actionResult?.actionSuccessful == false,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 30.dp),
                                enter = fadeIn() + scaleIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                ErrorPopup(
                                    message = actionResult?.messageStringId?.let { stringResource(id = it) }
                                        ?: ""
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}