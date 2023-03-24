package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.*
import vlad.dima.sales.R
import vlad.dima.sales.repository.UserRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.theme.Orange
import vlad.dima.sales.ui.theme.SalesTheme

class NotificationChatActivity : ComponentActivity() {

    private lateinit var viewModel: NotificationChatViewModel
    private lateinit var title: String
    private lateinit var description: String
    private var importance: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = intent.getStringExtra("title") ?: ""
        description = intent.getStringExtra("description") ?: ""
        importance = intent.getIntExtra("importance", 0)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDAO())

        viewModel = ViewModelProvider(
            this, NotificationChatViewModel.Factory(intent.getStringExtra("id") ?: "", repository)
        )[NotificationChatViewModel::class.java]

        viewModel.currentUserLD.observe(this) { user ->
            if (user != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.currentUserState.emit(user)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { stringId ->
                if (stringId != null) {
                    when (stringId) {
                        R.string.NotificationMessageError -> Toast.makeText(this@NotificationChatActivity, stringId, Toast.LENGTH_SHORT)
                            .show()
                        R.string.NotificationDeleted -> {
                            Toast.makeText(this@NotificationChatActivity, stringId, Toast.LENGTH_SHORT).show()
                            setResult(
                                RESULT_OK,
                                Intent().putExtra("isNotificationDeleted", true)
                            )
                            finish()
                        }
                    }
                }
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SalesTheme(defaultSystemBarsColor = false) {
                val uiController = rememberSystemUiController()
                uiController.setStatusBarColor(MaterialTheme.colors.surface)
                uiController.setNavigationBarColor(MaterialTheme.colors.surface)

                val currentUser by viewModel.currentUserState.collectAsState()
                val messages by viewModel.messages.collectAsState()
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val scrollState = rememberLazyListState()
                var previousScrollItem by remember { mutableStateOf(0) }
                val scrollUpVisibility by remember {
                    derivedStateOf {
                        if (scrollState.firstVisibleItemIndex != 0) {
                            (scrollState.firstVisibleItemIndex < previousScrollItem)
                        } else {
                            false
                        }.also {
                            previousScrollItem = scrollState.firstVisibleItemIndex
                        }
                    }
                }
                val scrollDownVisibility by remember {
                    derivedStateOf {
                        messages.isNotEmpty() && scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != scrollState.layoutInfo.totalItemsCount - 1
                    }
                }
                val coroutineScope = rememberCoroutineScope()
                val currentNotification by viewModel.currentNotification.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .systemBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isRefreshing,
                            enter = slideInVertically { -16 } + fadeIn(),
                            exit = slideOutVertically { -16 } + fadeOut(),
                            modifier = Modifier
                                .zIndex(1f)
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        ) {
                            CircularProgressIndicator()
                        }

                        if (messages.isEmpty()) {
                            Text(
                                text = "No messages",
                                color = MaterialTheme.colors.onBackground,
                                modifier = Modifier.align(
                                    Alignment.Center
                                )
                            )
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isRefreshing,
                            enter = slideInVertically { 16 } + fadeIn(),
                            exit = slideOutVertically { 16 } + fadeOut(),
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                state = scrollState
                            ) {
                                item {
                                    NotificationChatAppBar(
                                        title = title,
                                        description = description,
                                        importance = importance,
                                        isManager = currentUser.managerUID.isEmpty(),
                                        onDelete = {
                                            viewModel.deleteMessage()
                                        }
                                    )
                                }
                                items(
                                    items = messages
                                ) {
                                    Column {
                                        val previousMessageAuthorIndex = messages.indexOf(it) - 1
                                        if (it.authorUID != currentUser.userUID && (previousMessageAuthorIndex < 0 || messages[previousMessageAuthorIndex].authorUID != it.authorUID)) {
                                            Text(
                                                text = "${if (it.authorUID == currentNotification.managerUID) "✪" else ""} ${it.authorName}",
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colors.onBackground,
                                                modifier = Modifier.padding(
                                                    top = 32.dp,
                                                    start = 16.dp
                                                )
                                            )
                                        } else if (previousMessageAuthorIndex < 0 || messages[previousMessageAuthorIndex].authorUID != it.authorUID) {
                                            Spacer(modifier = Modifier.height(32.dp))
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 8.dp, end = 8.dp, top = 5.dp)
                                        ) {
                                            if (it.authorUID != currentUser.userUID) {
                                                Box(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Card(
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.rounded_corner_radius
                                                            )
                                                        ),
                                                        contentColor = MaterialTheme.colors.secondaryVariant
                                                    ) {
                                                        Text(
                                                            text = it.message,
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Box(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Card(
                                                        shape = RoundedCornerShape(
                                                            dimensionResource(
                                                                id = R.dimen.rounded_corner_radius
                                                            )
                                                        ),
                                                        contentColor = MaterialTheme.colors.primaryVariant,
                                                        modifier = Modifier.align(Alignment.CenterEnd)
                                                    ) {
                                                        Text(
                                                            text = it.message,
                                                            modifier = Modifier.padding(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {

                            androidx.compose.animation.AnimatedVisibility(
                                visible = scrollUpVisibility || scrollDownVisibility,
                                enter = slideInVertically { -16 } + fadeIn(),
                                exit = slideOutVertically { -16 } + fadeOut(),
                                modifier = Modifier
                                    .zIndex(1f)
                                    .align(Alignment.End)
                                    .padding(end = 8.dp, bottom = 8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (scrollUpVisibility) {
                                                scrollState.animateScrollToItem(0)
                                            } else if (scrollDownVisibility) {
                                                scrollState.animateScrollToItem(messages.size - 1)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colors.surface, CircleShape)
                                        .border(1.dp, MaterialTheme.colors.secondaryVariant, CircleShape)
                                        .size(50.dp),
                                ) {
                                    if (scrollUpVisibility) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowUpward,
                                            contentDescription = getString(
                                                R.string.GoToTop
                                            ),
                                            tint = MaterialTheme.colors.onSurface
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDownward,
                                            contentDescription = getString(
                                                R.string.GoToBottom
                                            ),
                                            tint = MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colors.surface
                                    )
                                    .padding(8.dp)
                            ) {
                                TextField(
                                    value = viewModel.message,
                                    onValueChange = { viewModel.message = it },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 18.sp
                                    ),
                                    placeholder = { Text(text = stringResource(id = R.string.SendMessage)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent
                                    )
                                )
                                AnimatedVisibility(visible = viewModel.message.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            viewModel.sendMessage()
                                        },
                                        enabled = viewModel.message.isNotEmpty()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Send,
                                            contentDescription = getString(
                                                R.string.SendMessage
                                            ),
                                            tint = MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                                LaunchedEffect(key1 = messages) {
                                    if (messages.isNotEmpty()) {
                                        scrollState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
