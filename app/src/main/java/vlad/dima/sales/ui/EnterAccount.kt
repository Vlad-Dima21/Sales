package vlad.dima.sales.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import vlad.dima.sales.R
import vlad.dima.sales.ui.theme.*
import kotlin.math.sign

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalesTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isSystemInDarkTheme()) DarkBackground else LightBackground)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(.9f)
//                            .fillMaxHeight(.7f)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(15.dp),
                        elevation = 5.dp,
                        backgroundColor = if (isSystemInDarkTheme()) DarkBackground else LightSurface,
                        border = if (isSystemInDarkTheme()) BorderStroke(2.dp, DarkSurface) else BorderStroke(0.dp, LightSurface)
                    ) {
                        EnterAccountNavigation()
                    }
                }
            }
        }
    }
}

private sealed class Screen(val route: String) {
    object LoginScreen : Screen("login")
    object SignUpScreen : Screen("sign_up")
}

@Composable
fun EnterAccountNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(route = Screen.LoginScreen.route) {
            LoginComposable(navController = navController)
        }
        composable(route = Screen.SignUpScreen.route) {
            SignUpComposable(navController = navController)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LoginComposable(navController: NavController) {
    var emailFieldState by remember {
        mutableStateOf("")
    }
    var passwordFieldState by remember {
        mutableStateOf("")
    }
    var passwordVisibleState by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val gradientColors = listOf(GreenPrimaryLight, GreenPrimary, GreenPrimaryDark, TealSecondaryDark, TealSecondary, TealSecondaryLight)
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
                value = emailFieldState,
                onValueChange = {
                    emailFieldState = it
                },
                label = {
                    Text(stringResource(id = R.string.Email))
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = passwordFieldState,
                onValueChange = {
                    passwordFieldState = it
                },
                label = {
                    Text(stringResource(R.string.Password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        /*TODO*/
                        Toast.makeText(context, "email: $emailFieldState   password: $passwordFieldState", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalTextApi::class)
@Composable
fun SignUpComposable(navController: NavController) {
    var emailFieldState by remember {
        mutableStateOf("")
    }
    var passwordFieldState by remember {
        mutableStateOf("")
    }
    var passwordVisibleState by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val gradientColors = listOf(GreenPrimaryLight, GreenPrimary, GreenPrimaryDark, TealSecondaryDark, TealSecondary, TealSecondaryLight)
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
                value = emailFieldState,
                onValueChange = {
                    emailFieldState = it
                },
                label = {
                    Text(stringResource(id = R.string.Email))
                },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = passwordFieldState,
                onValueChange = {
                    passwordFieldState = it
                },
                label = {
                    Text(stringResource(R.string.Password))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                        /*TODO*/
                        Toast.makeText(context, "email: $emailFieldState   password: $passwordFieldState", Toast.LENGTH_SHORT).show()
                    },
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
