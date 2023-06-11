package vlad.dima.sales

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import vlad.dima.sales.model.repository.SettingsRepository
import vlad.dima.sales.ui.enter_account.dataStore

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31])
class SettingsUnitTests {
    private var context: Context = ApplicationProvider.getApplicationContext()
    private var settingsRepository: SettingsRepository = SettingsRepository(context.dataStore)

    @Test
    fun check_isHintHidden() = runTest {
        settingsRepository.isHintHidden().test {
            assert(awaitItem() == null)
        }
        settingsRepository.toggleHintHidden()
        settingsRepository.isHintHidden().test {
            assert(awaitItem() == true)
        }
    }
}