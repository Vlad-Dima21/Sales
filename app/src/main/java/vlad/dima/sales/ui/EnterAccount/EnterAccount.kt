package vlad.dima.sales.ui.EnterAccount

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.ui.UIConstants.Companion.BORDER_WIDTH
import vlad.dima.sales.ui.UIConstants.Companion.ROUNDED_CORNER_RADIUS
import vlad.dima.sales.ui.theme.*

class EnterAccount : ComponentActivity() {

    private lateinit var viewModel: EnterAccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[EnterAccountViewModel::class.java]

        // set the app to be in fullscreen (status bar is no longer semi-transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // the activity observes the results of viewModel operations
        viewModel.actionResult.observe(this) { result ->
            if (!result.actionSuccessful) {
                    errorMessage = getString(result.messageStringId)
            } else {
                // TODO start new activity
                Toast.makeText(this, result.messageStringId, Toast.LENGTH_SHORT).show()
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
                Box(
                    modifier = backgroundModifier
                        .fillMaxSize()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(.9f)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
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
                        LaunchedEffect(key1 = errorMessage) {
                            delay(3000)
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

@Composable
fun EnterAccountNavigation(
    viewModel: EnterAccountViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(route = Screen.LoginScreen.route) {
            LoginComposable(navController, viewModel)
        }
        composable(route = Screen.SignUpScreen.route) {
            SignUpComposable(navController, viewModel)
        }
    }
}

// gradient colors used for card header text
val gradientColors = listOf(GreenPrimaryLight, GreenPrimary, GreenPrimaryDark, TealSecondaryDark, TealSecondary, TealSecondaryLight)

@OptIn(ExperimentalTextApi::class)
@Composable
fun LoginComposable(navController: NavController, viewModel: EnterAccountViewModel) {
    var passwordVisibleState by rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current
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
    val context = LocalContext.current
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
                shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS)
            )
            .border(
                width = BORDER_WIDTH,
                color = Color.Red,
                shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS)
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