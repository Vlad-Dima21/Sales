package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClientCard(
    client: Client, modifier: Modifier = Modifier, viewModel: SalesmanClientsViewModel
) {
    val context = LocalContext.current
    var expandedClient by viewModel.expandedClient
    val isExpanded by remember {
        derivedStateOf {
            expandedClient == client
        }
    }
    Card(
        onClick = {
            if (!isExpanded) {
                expandedClient = client
            } else {
                viewModel.startCreatingOrder(client)
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
        border = when (isExpanded) {
            true -> BorderStroke(1.dp, MaterialTheme.colors.primaryVariant)
            else -> null
        },
        elevation = dimensionResource(id = R.dimen.standard_elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = client.clientName, fontSize = 24.sp, modifier = Modifier.weight(1f)
                )
                IconText(icon = Icons.Filled.Payments, text = "${0}")
            }

            AnimatedVisibility(
                visible = isExpanded,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButtonWithIcon(
                        onClick = {
                            val intentUri = Uri.parse("geo:0,0?q=${client.address}")
                            val intent = Intent(Intent.ACTION_VIEW, intentUri)
                            context.startActivity(intent)
                        },
                        icon = Icons.Filled.Map,
                        text = client.address
                    )
                    TextButtonWithIcon(
                        onClick = {
                            val intent = Intent(ContactsContract.Intents.Insert.ACTION)
                            intent.type = ContactsContract.RawContacts.CONTENT_TYPE
                            intent
                                .putExtra(ContactsContract.Intents.Insert.NAME, client.contactName)
                                .putExtra(
                                    ContactsContract.Intents.Insert.PHONE,
                                    client.contactPhone
                                )
                                .putExtra(
                                    ContactsContract.Intents.Insert.COMPANY,
                                    client.clientName
                                )
                            context.startActivity(intent)
                        },
                        icon = Icons.Filled.Person,
                        text = client.contactName
                    )
                    TextButtonWithIcon(
                        onClick = {
                            val intentUri = Uri.parse("tel:${client.contactPhone}")
                            val intent = Intent(Intent.ACTION_DIAL, intentUri)
                            context.startActivity(intent)
                        },
                        icon = Icons.Filled.Call,
                        text = client.contactPhone
                    )
                }
            }
        }
    }
}


@Composable
fun IconText(
    icon: ImageVector, text: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colors.secondaryVariant
        )
        Text(
            text = text,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun TextButtonWithIcon(
    onClick: () -> Unit, icon: ImageVector, text: String
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.onSurface)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(ButtonDefaults.IconSize),
            tint = MaterialTheme.colors.secondaryVariant
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}
