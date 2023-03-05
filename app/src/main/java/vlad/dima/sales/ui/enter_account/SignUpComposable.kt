package vlad.dima.sales.ui.enter_account

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import vlad.dima.sales.R
import vlad.dima.sales.ui.theme.*

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