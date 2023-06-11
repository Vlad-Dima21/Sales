package vlad.dima.sales

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.enter_account.EnterAccountViewModel
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class EnterAccountUnitTests {

    private lateinit var userRepository: UserRepository
    private lateinit var database: SalesDatabase
    @Mock private lateinit var networkManager: NetworkManager
    private lateinit var enterAccountViewModel: EnterAccountViewModel

    @Before
    fun createDependencies() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SalesDatabase::class.java).build()

        val userDao = database.userDao()
        userRepository = UserRepository(userDao)
        networkManager = mock(NetworkManager::class.java)
        Mockito.`when`(networkManager.currentConnection)
            .thenReturn(MutableStateFlow(NetworkManager.NetworkStatus.Available))

        val auth = mock(FirebaseAuth::class.java)
        val usersCollection = mock(CollectionReference::class.java)

        enterAccountViewModel = EnterAccountViewModel(userRepository, networkManager, auth, usersCollection)
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun check_loginNoCredentials() {
        val inputError = enterAccountViewModel.inputError
        enterAccountViewModel.loginUser()
        Assert.assertSame(inputError.value, EnterAccountViewModel.InvalidFields.All)
    }

    @Test
    fun check_loginInvalidEmail() {
        val inputError = enterAccountViewModel.inputError
        enterAccountViewModel.apply {
            emailFieldState = "invalid_email"
            passwordFieldState = "password"
        }.loginUser()
        Assert.assertSame(inputError.value, EnterAccountViewModel.InvalidFields.Email)
    }

    @Test
    fun check_loginWithValidCredentials() {
        val inputError = enterAccountViewModel.inputError
        enterAccountViewModel.apply {
            emailFieldState = "username@email.com"
            passwordFieldState = "password"
        }.loginUser()
        Assert.assertSame(inputError.value, EnterAccountViewModel.InvalidFields.None)
    }
}