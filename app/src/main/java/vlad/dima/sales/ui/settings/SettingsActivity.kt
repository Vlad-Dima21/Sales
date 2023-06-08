package vlad.dima.sales.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.StateFlow
import vlad.dima.sales.model.repository.SettingsRepository
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.enter_account.dataStore
import vlad.dima.sales.ui.theme.SalesTheme

class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var popupState: StateFlow<SettingsViewModel.Popup>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userRepository = UserRepository(SalesDatabase.getDatabase(this).userDao())
        val networkManager = NetworkManager(applicationContext)
        val settingsRepository = SettingsRepository(dataStore)

        viewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(networkManager, userRepository, settingsRepository)
        )[SettingsViewModel::class.java]

        popupState = viewModel.popupState

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SalesTheme(defaultSystemBarsColor = false) {
                val uiController = rememberSystemUiController()
                with(uiController) {
                    setStatusBarColor(Color.Transparent, darkIcons = MaterialTheme.colors.isLight)
                    setNavigationBarColor(MaterialTheme.colors.background)
                }
                SettingsPage(viewModel = viewModel)
            }
        }
    }

    override fun finish() {
        if (popupState.value == SettingsViewModel.Popup.Hidden) {
            super.finish()
        } else {
            viewModel.setPopupState(SettingsViewModel.Popup.Hidden)
        }
    }
}