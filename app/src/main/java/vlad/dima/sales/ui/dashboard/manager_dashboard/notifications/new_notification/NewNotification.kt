package vlad.dima.sales.ui.dashboard.manager_dashboard.notifications.new_notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vlad.dima.sales.R
import vlad.dima.sales.ui.dashboard.common.notifications.Notification
import vlad.dima.sales.ui.theme.*

const val NOTIFICATION_ERROR = "Post notification error"
class NewNotification : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationsCollection = Firebase.firestore.collection("notifications")
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
        var isError by mutableStateOf(false)

        setContent {
            SalesTheme {
                var title by rememberSaveable {
                    mutableStateOf("")
                }
                var description by rememberSaveable {
                    mutableStateOf("")
                }
                var importance = rememberSaveable {
                    mutableStateOf(0)
                }
                val keyboardController = LocalSoftwareKeyboardController.current
                val coroutineScope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colors.primary)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = getString(R.string.NewNotificationAppBar),
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        IconButton(
                            onClick = {
                                if (title.isEmpty() || description.isEmpty()) {
                                    isError = true
                                    return@IconButton
                                }
                                if (currentUserUID != null) {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val newNotification = Notification(
                                                title = title,
                                                description = description,
                                                managerUID = currentUserUID,
                                                importance = importance.value
                                            )
                                            try {
                                                notificationsCollection.add(newNotification).await()
                                            } catch (e: Exception) {
                                                Log.d(NOTIFICATION_ERROR, e.stackTraceToString())
                                                Toast.makeText(
                                                    this@NewNotification,
                                                    R.string.SystemError,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@withContext
                                            }
                                            setResult(
                                                RESULT_OK,
                                                Intent().putExtra("isNotificationAdded", true)
                                            )
                                            finish()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(imageVector = Icons.Filled.AddAlert, contentDescription = stringResource(
                                id = R.string.AddNotification
                            ))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = getString(R.string.NotificationDetailsLabel),
                            color = MaterialTheme.colors.onBackground,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth()
                        )
                        TextField(
                            value = title,
                            placeholder = { Text(getString(R.string.NotificationTitlePlaceholder)) },
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = if (MaterialTheme.colors.isLight) Color.Black else Color.White,
                                fontSize = 16.sp
                            ),
                            isError = isError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        TextField(
                            value = description,
                            placeholder = { Text(getString(R.string.NotificationDescPlaceholder)) },
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = if (MaterialTheme.colors.isLight) Color.Black else Color.White,
                                fontSize = 16.sp
                            ),
                            isError = isError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = getString(R.string.NotificationImportanceLabel),
                            color = when (importance.value) {
                                 1 -> Orange
                                 2 -> Color.Red
                                 else -> MaterialTheme.colors.onBackground
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Divider(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth()
                        )
                        OptionGroup(
                            items = listOf(getString(R.string.NormalImportance), getString(R.string.HightImportance), getString(
                                R.string.AlertImportance)
                            ),
                            selectedIndex = importance
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionGroup(
    items: List<String>,
    selectedIndex: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            ClickableText(
                text = AnnotatedString(text = item),
                modifier = if (index == selectedIndex.value) {
                    Modifier.background(
                        color = LightGray, shape = RoundedCornerShape(
                            dimensionResource(id = R.dimen.rounded_corner_radius)
                        )
                    )
                } else {
                    Modifier.background(color = MaterialTheme.colors.background)
                }
                    .width(80.dp)
                    .padding(10.dp),
                style = TextStyle(
                    color = if (index == selectedIndex.value) Color.Black else MaterialTheme.colors.onBackground,
                    fontWeight = if (index == selectedIndex.value) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
                onClick = {
                    selectedIndex.value = index
                }
            )
        }
    }
}