package vlad.dima.sales.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.runBlocking
import vlad.dima.sales.R
import vlad.dima.sales.model.User
import vlad.dima.sales.network.NetworkManager
import vlad.dima.sales.ui.enter_account.EnterAccountActivity
import vlad.dima.sales.ui.enter_account.dataStore

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel
) {
    val localContext = LocalContext.current
    val currentUser by viewModel.currentUserWithInfo.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val popupState by viewModel.popupState.collectAsState()
    val popupBackgroundInteractionSource = remember {
        MutableInteractionSource()
    }
    val unassignedSalesmen by viewModel.unassignedSalesmen.collectAsState()
    val teamSalesmen by viewModel.teamSalesmen.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            AnimatedVisibility(visible = networkStatus != NetworkManager.NetworkStatus.Available) {
                Text(
                    text = stringResource(id = R.string.CheckConnection),
                    color = MaterialTheme.colors.onError,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.error)
                        .statusBarsPadding()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                currentUser?.let { UserHeader(user = it) }
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.Options),
                        fontSize = 28.sp,
                        color = MaterialTheme.colors.onBackground.copy(.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_radius)),
                    ) {
                        Column {
                            if (currentUser?.managerUID == "") {
                                ManagerSettings(
                                    addSalesman = {
                                        viewModel.setPopupState(SettingsViewModel.Popup.ManagerAddSalesmen)
                                    },
                                    manageTeam = {
                                        viewModel.setPopupState(SettingsViewModel.Popup.ManagerManageTeam)
                                    }
                                )
                            } else {
                                SalesmanSettings(
                                    viewModel.salesmanPreferredImportance,
                                    setPreferredImportance = {
                                        viewModel.setPreferredImportance(it)
                                    },
                                    onViewTeam = {
                                        viewModel.setPopupState(SettingsViewModel.Popup.SalesmanViewTeamMembers)
                                    }
                                )
                            }
                            Divider(Modifier.fillMaxWidth())
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.logOutUser()
                                        localContext.startActivity(
                                            Intent(localContext, EnterAccountActivity::class.java)
                                                .apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                }
                                        )
                                        runBlocking {
                                            (localContext as Activity).dataStore.edit {
                                                it.clear()
                                            }
                                        }
                                        (localContext as Activity).finish()
                                    }
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.LogOut),
                                    color = Color.Red,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Logout,
                                    contentDescription = stringResource(
                                        id = R.string.LogOut,
                                    ),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .let {
                if (popupState != SettingsViewModel.Popup.Hidden)
                    it
                        .background(Color.Black.copy(.5f))
                        .clickable(
                            interactionSource = popupBackgroundInteractionSource,
                            indication = null
                        ) {
                            viewModel.setPopupState(SettingsViewModel.Popup.Hidden)
                        }
                else
                    it
            }
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 50.dp),
            visible = popupState != SettingsViewModel.Popup.Hidden,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -30 })
        ) {
            when (popupState) {
                SettingsViewModel.Popup.Hidden -> {}
                
                SettingsViewModel.Popup.ManagerAddSalesmen ->
                    ManagerAddSalesmanPopup(
                        manager = currentUser ?: User(),
                        salesmen = unassignedSalesmen,
                        networkStatus = networkStatus,
                        onConfirmSalesman = {
                            viewModel.addSalesmanToTeam(it)
                        },
                        onFinished = {
                            viewModel.setPopupState(SettingsViewModel.Popup.Hidden)
                            viewModel.managerInit()
                        }
                    )

                SettingsViewModel.Popup.ManagerManageTeam ->
                    ManagerManageTeam(salesmen = teamSalesmen)

                else -> {
                    SalesmanViewTeam(salesmen = teamSalesmen)
                }
            }
        }
    }
}