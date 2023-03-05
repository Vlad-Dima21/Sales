package vlad.dima.sales.ui.EnterAccount

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.ui.Dashboard.SalesmanDashboard.SalesmanDashboardActivity
import vlad.dima.sales.ui.theme.*

class EnterAccountActivity : ComponentActivity() {

    private lateinit var viewModel: EnterAccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[EnterAccountViewModel::class.java]

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
                            delay(1000)
                            errorMessage = ""
                        }
                    }
                }
            }
        }
    }
}

var errorMessage by mutableStateOf("")

private sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object SignUpScreen : Screen("sign_up")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnterAccountNavigation(
    viewModel: EnterAccountViewModel
) {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(
            route = Screen.LoginScreen.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            }
        ) {
            LoginComposable(navController, viewModel)
        }
        composable(
            route = Screen.SignUpScreen.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            }
        ) {
            SignUpComposable(navController, viewModel)
        }
    }
}

// gradient colors used for card header text
val gradientColors = listOf(GreenPrimaryLight, GreenPrimary, GreenPrimaryDark, TealSecondaryDark, TealSecondary, TealSecondaryLight)

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginComposable(navController: NavController, viewModel: EnterAccountViewModel) {
    var passwordVisibleState by rememberSaveable {
        mutableStateOf(false)
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.padding(15.dp),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp),
            text = stringResource(R.string.Login),
            color = GreenPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    colors = gradientColors
                )
            )
        )
        Column(
            modifier = Modifier
                .padding(top = 100.dp)
        ) {
            OutlinedTextField(
                value = viewModel.emailFieldState,
                onValueChange = {
                    viewModel.emailFieldState = it
                },
                label = {
                    Text(stringResource(id = R.string.Email))
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = viewModel.inputError,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.passwordFieldState,
                onValueChange = {
                    viewModel.passwordFieldState = it
                },
                label = {
                    Text(stringResource(R.string.Password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    viewModel.loginUser()
                }),
                visualTransformation = if (passwordVisibleState) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                   val toggle = if (passwordVisibleState) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = {
                        passwordVisibleState = !passwordVisibleState
                    }) {
                        Icon(
                            imageVector = toggle,
                            contentDescription = stringResource(R.string.ToggleVisibility)
                        )
                    }
                },
                singleLine = true,
                isError = viewModel.inputError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.loginUser()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.Login),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    ClickableText(
                        text = AnnotatedString(stringResource(R.string.SignUp)),
                        style = TextStyle(color = if(isSystemInDarkTheme()) Color.White else Color.Black, textDecoration = TextDecoration.Underline),
                        onClick = {
                            viewModel.inputError = false
                            errorMessage = ""
                            navController.navigate(Screen.SignUpScreen.route) {
                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpComposable(navController: NavController, viewModel: EnterAccountViewModel) {
    var passwordVisibleState by rememberSaveable {
        mutableStateOf(false)
    }
    val gradientColors = listOf(GreenPrimaryLight, GreenPrimary, GreenPrimaryDark, TealSecondaryDark, TealSecondary, TealSecondaryLight)
    val virtualKeyboard = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier.padding(15.dp),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp),
            text = stringResource(R.string.SignUp),
            color = GreenPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
        )
        Column(
            modifier = Modifier
                .padding(top = 100.dp)
        ) {
            OutlinedTextField(
                value = viewModel.emailFieldState,
                onValueChange = {
                    viewModel.emailFieldState = it
                },
                label = {
                    Text(stringResource(id = R.string.Email))
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = viewModel.inputError,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = viewModel.usernameFieldState,
                onValueChange = {
                    viewModel.usernameFieldState = it
                },
                label = {
                    Text(stringResource(id = R.string.Username))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                singleLine = true,
                isError = viewModel.inputError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            OutlinedTextField(
                value = viewModel.passwordFieldState,
                onValueChange = {
                    viewModel.passwordFieldState = it
                },
                label = {
                    Text(stringResource(R.string.Password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    virtualKeyboard?.hide()
                    viewModel.signUpUser()
                }),
                visualTransformation = if (passwordVisibleState) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val toggle = if (passwordVisibleState) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = {
                        passwordVisibleState = !passwordVisibleState
                    }) {
                        Icon(
                            imageVector = toggle,
                            contentDescription = stringResource(R.string.ToggleVisibility)
                        )
                    }
                },
                singleLine = true,
                isError = viewModel.inputError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.signUpUser() },
                ) {
                    Text(
                        text = stringResource(R.string.SignUp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    ClickableText(
                        text = AnnotatedString(stringResource(R.string.Login)),
                        style = TextStyle(color = if(isSystemInDarkTheme()) Color.White else Color.Black, textDecoration = TextDecoration.Underline),
                        onClick = {
                            errorMessage = ""
                            viewModel.inputError = false
                            navController.navigate(Screen.LoginScreen.route) {
                                popUpTo(Screen.SignUpScreen.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorPopup(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSystemInDarkTheme()) DarkSurface else LightSurface,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
            )
            .border(
                width = dimensionResource(id = R.dimen.border_width),
                color = Color.Red,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius))
            )
            .fillMaxWidth(0.8f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center),
            color = if (isSystemInDarkTheme()) Color.White else Color.Black
        )
    }
}