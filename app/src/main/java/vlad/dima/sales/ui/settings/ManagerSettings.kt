package vlad.dima.sales.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import vlad.dima.sales.R
import vlad.dima.sales.model.User
import vlad.dima.sales.network.NetworkManager

@Composable
fun ColumnScope.ManagerSettings(
    addSalesman: () -> Unit,
    manageTeam: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                addSalesman()
            }
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.AddNewMembers),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(
                id = R.string.AddNewMembers,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
    Divider(Modifier.fillMaxWidth())
    Row(
        modifier = Modifier
            .clickable {
                manageTeam()
            }
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.ManageTeam),
            modifier = Modifier.weight(1f),
            fontSize = 20.sp
        )
        Icon(
            imageVector = Icons.Rounded.Menu,
            contentDescription = stringResource(
                id = R.string.ManageTeam,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun ManagerAddSalesmanPopup(
    manager: User,
    salesmen: List<User>,
    networkStatus: NetworkManager.NetworkStatus,
    onConfirmSalesman: (salesman: User) -> Unit,
    onFinished: () -> Unit
) {

    val localContext = LocalContext.current

    var selectedSalesman by remember {
        mutableStateOf<User?>(null)
    }

    var showConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val contentInteractionSource = remember {
        MutableInteractionSource()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = contentInteractionSource,
                indication = null
            ) {}
            .animateContentSize(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            width = 1.dp,
            MaterialTheme.colors.secondary.copy(.7f)
        )
    ) {
        if (salesmen.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.NoAvailableSalesmen),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        else if (selectedSalesman == null) {
            LazyColumn {
                items(
                    items = salesmen,
                    key = { it.userUID }
                ) {
                    if (it != salesmen.first()) {
                        Divider(Modifier.fillMaxWidth())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSalesman = it }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = it.fullName, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${it.email})",
                            color = MaterialTheme.colors.onSurface.copy(.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else if (!showConfirmation) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.AddMember, selectedSalesman!!.fullName),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(.7f)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (networkStatus == NetworkManager.NetworkStatus.Available) {
                                onConfirmSalesman(selectedSalesman!!)
                                showConfirmation = true
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.Yes))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedSalesman = null
                        }
                    ) {
                        Text(text = stringResource(id = R.string.Cancel))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.SendEmail, selectedSalesman!!.fullName),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(.7f)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(selectedSalesman!!.email))
                                putExtra(Intent.EXTRA_SUBJECT, localContext.getString(R.string.EmailTemplateSubject))
                                putExtra(Intent.EXTRA_TEXT, localContext.getString(R.string.EmailTemplateBody, selectedSalesman!!.fullName, manager.fullName))
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                localContext.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(localContext, R.string.NoEmailAppFound, Toast.LENGTH_SHORT).show()
                            }
                            onFinished()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.Yes))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFinished()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.Cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerManageTeam(
    salesmen: List<User>
) {
    val localContext = LocalContext.current
    val contentInteractionSource = remember {
        MutableInteractionSource()
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = contentInteractionSource,
                indication = null
            ) {}
            .animateContentSize(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        border = BorderStroke(
            width = 1.dp,
            MaterialTheme.colors.secondary.copy(.7f)
        )
    ) {
        if (salesmen.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.NoSalesmenInTeam),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(
                    items = salesmen,
                    key = { it.userUID }
                ) {
                    if (it != salesmen.first()) {
                        Divider(Modifier.fillMaxWidth())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(it.email))
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    localContext.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(localContext, R.string.NoEmailAppFound, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = it.fullName, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "(${it.email})",
                                color = MaterialTheme.colors.onSurface.copy(.7f)
                            )
                        }
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = stringResource(id = R.string.Email)
                        )
                    }
                }
            }
        }
    }
}