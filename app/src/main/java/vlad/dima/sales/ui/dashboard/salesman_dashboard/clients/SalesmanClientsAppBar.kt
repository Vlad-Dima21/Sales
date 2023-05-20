package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.ui.composables.SortButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SalesmanClientsAppBar(
    viewModel: SalesmanClientsViewModel
) {
    val sort by viewModel.sort.collectAsState()
    val searchText = viewModel.search
    var searchExtended by rememberSaveable {
        mutableStateOf(false)
    }
    val sortWeight by animateFloatAsState(
        targetValue = if (searchExtended) .001f else 1f,
        label = ""
    )
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = dimensionResource(id = R.dimen.standard_elevation),
        color = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.DashboardClients),
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)))
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colors.surface)
                    .padding(4.dp)
                    .fillMaxWidth(.9f)
            ) {
                Row(
                    Modifier
                        .height(IntrinsicSize.Min)
                        .animateContentSize()
                ) {
                    SortButton(
                        modifier = Modifier
                            .weight(sortWeight)
                            .fillMaxHeight()
                            .heightIn(max = 48.dp)
                            .padding(end = 5.dp),
                        label = stringResource(id = R.string.SortByOrders),
                        state = sort,
                        onClick = {
                            viewModel.toggleSort()
                        }
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onFocusEvent {
                                searchExtended = it.isFocused
                            },
                        value = searchText,
                        onValueChange = {
                            viewModel.search = it
                        },
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
                        maxLines = 1,
                        singleLine = true,
                        placeholder = {
                            Text(stringResource(id = R.string.SearchClient))
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboard?.hide()
                            focusManager.clearFocus()
                            viewModel.changeSearch()
                        }),
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search = ""; viewModel.changeSearch(); focusManager.clearFocus() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Cancel,
                                        contentDescription = stringResource(
                                            id = R.string.ClearText
                                        )
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(textColor = MaterialTheme.colors.onSurface)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}