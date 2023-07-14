package vlad.dima.sales.ui.dashboard.common.notifications.notification_chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import vlad.dima.sales.R
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.model.repository.UserRepository
import vlad.dima.sales.model.room.SalesDatabase
import vlad.dima.sales.ui.theme.SalesTheme
import vlad.dima.sales.ui.theme.extra
import vlad.dima.sales.utils.DateTime

class NotificationChatActivity : ComponentActivity() {

    private lateinit var viewModel: NotificationChatViewModel

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class,
        ExperimentalFoundationApi::class, ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = UserRepository(SalesDatabase.getDatabase(this).userDao())
        val networkManager = NetworkManager(applicationContext)

        viewModel = ViewModelProvider(
            this, NotificationChatViewModel.Factory(intent.getStringExtra("id") ?: "", repository)
        )[NotificationChatViewModel::class.java]

        lifecycleScope.launch {
            viewModel.error.collect { stringId ->
                if (stringId != null) {
                    when (stringId) {
                        R.string.NotificationMessageError -> Toast.makeText(
                            this@NotificationChatActivity,
                            stringId,
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        R.string.NotificationDeleted -> {
                            Toast.makeText(
                                this@NotificationChatActivity,
                                stringId,
                                Toast.LENGTH_SHORT
                            ).show()
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
                val networkStatus by networkManager.currentConnection.collectAsState(NetworkManager.NetworkStatus.Available)
                val messageCardColor = MaterialTheme.colors.surface
                val systemBarsColor by animateColorAsState(
                    if (networkStatus != NetworkManager.NetworkStatus.Available) MaterialTheme.colors.error else MaterialTheme.colors.surface,
                    label = "statusBarColor"
                )

                val uiController = rememberSystemUiController()
                uiController.setStatusBarColor(systemBarsColor)
                uiController.setNavigationBarColor(MaterialTheme.colors.surface)

                val currentUser by viewModel.currentUser.collectAsState()
                val messages by viewModel.messages.collectAsState()
                val groupedMessages by viewModel.groupedMessages.collectAsState()
                val currentResultPosition by viewModel.currentResultPosition.collectAsState()
                var selectedMessageId by rememberSaveable {
                    mutableStateOf("")
                }
                var isSearching by rememberSaveable {
                    mutableStateOf(false)
                }
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                val scrollState = rememberLazyListState()
                val keyboardController = LocalSoftwareKeyboardController.current
                val localDensity = LocalDensity.current
                val isImeShown by rememberUpdatedState(
                    WindowInsets.isImeVisible && WindowInsets.ime.getBottom(localDensity) >= WindowInsets.imeAnimationTarget.getBottom(localDensity).toFloat() * 9 / 10
                )
                var messagesLoaded by rememberSaveable {
                    mutableStateOf(false)
                }
                var oldMessageSize by rememberSaveable {
                    mutableStateOf(0)
                }
                val extraColor = MaterialTheme.colors.extra
                var previousScrollItem by remember { mutableStateOf(0) }
                val scrollUpVisibility by remember {
                    derivedStateOf {
                        if (scrollState.firstVisibleItemIndex != 0) {
                            (scrollState.firstVisibleItemIndex < previousScrollItem) && !isImeShown
                        } else {
                            false
                        }.also {
                            previousScrollItem = scrollState.firstVisibleItemIndex
                        }
                    }
                }
                val scrollDownVisibility by remember(messages, scrollState) {
                    derivedStateOf {
                        val lastIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        val itemsCount = scrollState.layoutInfo.totalItemsCount
                        messages.isNotEmpty() && !((itemsCount - 1) downTo (itemsCount - 3)).contains(lastIndex) && !isImeShown
                    }
                }
                val coroutineScope = rememberCoroutineScope()
                val currentNotification by viewModel.currentNotification.collectAsState()
                val focusManager = LocalFocusManager.current
                LaunchedEffect(messages.size) {
                    val lastIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    val itemsCount = scrollState.layoutInfo.totalItemsCount
                    if (messages.isNotEmpty()) {
                        if (!messagesLoaded) {
                            oldMessageSize = messages.size
                            messagesLoaded = true
                        }
                        if (((itemsCount - 1) downTo (itemsCount - 4)).contains(lastIndex)) {
                            oldMessageSize = messages.size
                            scrollState.animateScrollToItem(itemsCount - 1)
                        }
                    }
                }
                LaunchedEffect(scrollDownVisibility) {
                    if (!scrollDownVisibility) {
                        oldMessageSize = messages.size
                    }
                }
                LaunchedEffect(isImeShown) {
                    val itemsCount = scrollState.layoutInfo.totalItemsCount
                    if (isImeShown && messages.isNotEmpty()) {
                        scrollState.animateScrollToItem(itemsCount - 1)
                    }
                }
                LaunchedEffect(currentResultPosition) {
                    if (currentResultPosition.second > 0) {
                        scrollState.animateScrollToItem(currentResultPosition.second)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .systemBarsPadding()
                        .imePadding()
                ) {
                    AnimatedVisibility(visible = networkStatus != NetworkManager.NetworkStatus.Available) {
                        Text(
                            text = stringResource(id = R.string.CheckConnection),
                            color = MaterialTheme.colors.onError,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(systemBarsColor)
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    AnimatedVisibility(
                        visible = isRefreshing,
                        enter = slideInVertically { -16 } + fadeIn(),
                        exit = slideOutVertically { -16 } + fadeOut(),
                        modifier = Modifier
                            .zIndex(1f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp)
                    ) {
                        CircularProgressIndicator()
                    }

                    AnimatedVisibility(
                        modifier = Modifier.weight(1f),
                        visible = !isRefreshing,
                        enter = slideInVertically { 16 } + fadeIn(),
                        exit = slideOutVertically { 16 } + fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                state = scrollState
                            ) {
                                item {
                                    NotificationChatAppBar(
                                        notification = currentNotification,
                                        isManager = currentUser.managerUID.isEmpty(),
                                        onDelete = {
                                            viewModel.deleteNotification()
                                        }
                                    )
                                }
                                if (messages.isEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            text = getString(R.string.NoMessages),
                                            color = MaterialTheme.colors.onBackground
                                        )
                                    }
                                }
                                groupedMessages.forEachIndexed { index, (dateString, messages) ->
                                    item {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                    stickyHeader {
                                        Card(
                                            modifier = Modifier
                                                .padding(5.dp),
                                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
                                            onClick = {
                                                coroutineScope.launch {
                                                    scrollState.animateScrollToItem(1 + index + groupedMessages.subList(0, index).fold(0) { acc, (_, list) -> acc + list.size})
                                                }
                                            },
                                            elevation = 1.dp
                                        ) {
                                            Text(
                                                text = when(dateString == viewModel.currentFormattedDay) {
                                                    true -> stringResource(R.string.Today)
                                                    else -> dateString
                                                 },
                                                modifier = Modifier.padding(8.dp),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                    items(
                                        items = messages,
                                        key = { it.notificationMessageId }
                                    ) {
                                        val previousMessageAuthorIndex = messages.indexOf(it) - 1
                                        val nextMessageAuthorIndex = previousMessageAuthorIndex + 2
                                        val isStartOfNewGroup = previousMessageAuthorIndex < 0 || messages[previousMessageAuthorIndex].authorUID != it.authorUID
                                        val isEndOfGroup = nextMessageAuthorIndex == messages.size || messages[nextMessageAuthorIndex].authorUID != it.authorUID
                                        Column {
                                            if (it.authorUID != currentUser.userUID && isStartOfNewGroup) {
                                                Text(
                                                    text = "${if (it.authorUID == currentNotification.managerUID) "✪" else ""} ${it.authorName}",
                                                    fontSize = 18.sp,
                                                    color = MaterialTheme.colors.onBackground,
                                                    modifier = Modifier.padding(
                                                        top = 16.dp,
                                                        start = 8.dp
                                                    )
                                                )
                                            } else if (isStartOfNewGroup) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 8.dp, end = 8.dp, top = 5.dp)
                                            ) {
                                                if (it.authorUID != currentUser.userUID) {
                                                    Box(
                                                        modifier = Modifier.weight(3f)
                                                    ) {
                                                        Column {
                                                            Card(
                                                                shape = RoundedCornerShape(
                                                                    dimensionResource(
                                                                        id = R.dimen.rounded_corner_radius
                                                                    )
                                                                ),
                                                                contentColor = MaterialTheme.colors.secondaryVariant,
                                                                modifier = Modifier
                                                                    .let { modifier ->
                                                                        val color =
                                                                            if (currentResultPosition.first == it.notificationMessageId) extraColor else messageCardColor
                                                                        if (isStartOfNewGroup)
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        0f,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                        else if (isEndOfGroup)
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        0f,
                                                                                        0f
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                        else
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        0f,
                                                                                        0f
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                    }
                                                                    .widthIn(min = 50.dp),
                                                                border = if (currentResultPosition.first == it.notificationMessageId) BorderStroke(1.dp, MaterialTheme.colors.extra) else null,
                                                                elevation = 0.dp,
                                                                onClick = {
                                                                    selectedMessageId = when {
                                                                        selectedMessageId != it.notificationMessageId -> it.notificationMessageId
                                                                        else -> ""
                                                                    }
                                                                }
                                                            ) {
                                                                Box {
                                                                    Text(
                                                                        text = it.message,
                                                                        modifier = Modifier
                                                                            .padding(
                                                                                16.dp
                                                                            )
                                                                            .align(Alignment.Center)
                                                                    )
                                                                }
                                                            }
                                                            AnimatedVisibility(visible = selectedMessageId == it.notificationMessageId) {
                                                                Text(
                                                                    text = DateTime.getTime(it.sendDate),
                                                                    color = MaterialTheme.colors.onBackground,
                                                                    modifier = Modifier.padding(start = 5.dp, top = 5.dp),
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.weight(1f))
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Box(
                                                        modifier = Modifier.weight(3f)
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.End,
                                                            modifier = Modifier.align(Alignment.TopEnd)
                                                        ) {
                                                            Card(
                                                                shape = RoundedCornerShape(
                                                                    dimensionResource(
                                                                        id = R.dimen.rounded_corner_radius
                                                                    )
                                                                ),
                                                                contentColor = MaterialTheme.colors.primaryVariant,
                                                                modifier = Modifier
                                                                    .let { modifier ->
                                                                        val color =
                                                                            if (currentResultPosition.first == it.notificationMessageId) extraColor else messageCardColor
                                                                        if (isStartOfNewGroup)
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        size.width / 2,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                        else if (isEndOfGroup)
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        size.width / 2,
                                                                                        0f
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height / 2
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                        else
                                                                            modifier.drawBehind {
                                                                                drawRoundRect(
                                                                                    color,
                                                                                    topLeft = Offset(
                                                                                        size.width / 2,
                                                                                        0f
                                                                                    ),
                                                                                    size = Size(
                                                                                        size.width / 2,
                                                                                        size.height
                                                                                    ),
                                                                                    cornerRadius = CornerRadius(
                                                                                        10f,
                                                                                        10f
                                                                                    )
                                                                                )
                                                                            }
                                                                    }
                                                                    .widthIn(min = 50.dp),
                                                                border = if (currentResultPosition.first == it.notificationMessageId) BorderStroke(1.dp, MaterialTheme.colors.extra) else null,
                                                                elevation = 0.dp,
                                                                onClick = {
                                                                    selectedMessageId = when {
                                                                        selectedMessageId != it.notificationMessageId -> it.notificationMessageId
                                                                        else -> ""
                                                                    }
                                                                }
                                                            ) {
                                                                Box {
                                                                    Text(
                                                                        text = it.message,
                                                                        modifier = Modifier
                                                                            .padding(
                                                                                16.dp
                                                                            )
                                                                            .align(Alignment.Center)
                                                                    )
                                                                }
                                                            }
                                                            AnimatedVisibility(visible = selectedMessageId == it.notificationMessageId) {
                                                                Text(
                                                                    text = DateTime.getTime(it.sendDate),
                                                                    color = MaterialTheme.colors.onBackground,
                                                                    modifier = Modifier.padding(end = 5.dp, top = 5.dp),
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(10.dp)) }
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = scrollUpVisibility || scrollDownVisibility,
                                enter = slideInVertically { -16 } + fadeIn(),
                                exit = slideOutVertically { -16 } + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 8.dp, bottom = 8.dp)
                            ) {
                                Box {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (scrollUpVisibility) {
                                                    scrollState.animateScrollToItem(0)
                                                } else if (scrollDownVisibility) {
                                                    scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount)
                                                    oldMessageSize = messages.size
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .background(MaterialTheme.colors.surface, CircleShape)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colors.secondaryVariant,
                                                CircleShape
                                            )
                                            .size(60.dp),
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
                                    if (messagesLoaded && oldMessageSize < messages.size) {
                                        Text(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .drawBehind {
                                                    drawCircle(
                                                        color = extraColor,
                                                        radius = this.size.maxDimension
                                                    )
                                                },
                                            text = (messages.size - oldMessageSize).toString(),
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = viewModel.message.isEmpty()) {
                            IconButton(
                                onClick = {
                                    isSearching = !isSearching
                                    viewModel.resetSearch()
                                }
                            ) {
                                Icon(
                                    imageVector = if (!isSearching) Icons.Rounded.Search else Icons.Rounded.Message,
                                    contentDescription = stringResource(id = R.string.Search),
                                    tint = MaterialTheme.colors.onBackground
                                )
                            }
                        }
                            OutlinedTextField(
                                value = if (!isSearching) viewModel.message else viewModel.search,
                                onValueChange = { newText ->
                                    if (newText.isEmpty() || newText.isNotBlank()) {
                                        if (!isSearching) {
                                            viewModel.message = newText
                                        } else {
                                            viewModel.search = newText
                                        }
                                    }
                                },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.onSurface,
                                    fontSize = 18.sp
                                ),
                                placeholder = {
                                    if (!isSearching) {
                                        Text(text = stringResource(id = R.string.SendMessage))
                                    } else {
                                        Text(text = stringResource(id = R.string.Search))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.Transparent
                                ),
                                trailingIcon =
                                    if (isSearching && viewModel.noResults == 0) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Clear,
                                                contentDescription = null,
                                                tint = MaterialTheme.colors.error
                                            )
                                        }
                                    } else if (isSearching && viewModel.noResults > 0) {
                                        {
                                            Text(text = "${viewModel.currentResult + 1}/${viewModel.noResults}")
                                        }
                                    } else {
                                        null
                                    },
                                maxLines = if (!isSearching) 4 else 2,
                                keyboardOptions = when (!isSearching) {
                                    true -> KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Done
                                    )
                                    else -> KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Search
                                    )
                                },
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                    onSearch = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.searchMessages()
                                    }
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                            AnimatedVisibility(
                                visible = viewModel.message.isNotEmpty(),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                IconButton(
                                    modifier = Modifier.background(
                                        color = when (networkStatus == NetworkManager.NetworkStatus.Available) {
                                            true -> MaterialTheme.colors.primaryVariant
                                            else -> MaterialTheme.colors.primaryVariant.copy(.5f)
                                        },
                                        shape = CircleShape
                                    ),
                                    onClick = {
                                        coroutineScope.launch {
                                            oldMessageSize++
                                            viewModel.sendMessage().join()
                                        }
                                    },
                                    enabled = viewModel.message.isNotEmpty() && networkStatus == NetworkManager.NetworkStatus.Available
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = getString(
                                            R.string.SendMessage
                                        ),
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                }
                            }
                            if (viewModel.noResults > 0) {
                                IconButton(onClick = { viewModel.goToSearchResult(1) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                }
                                IconButton(onClick = { viewModel.goToSearchResult(-1) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                }
                            }
                        }
                }
            }
        }
    }
}
