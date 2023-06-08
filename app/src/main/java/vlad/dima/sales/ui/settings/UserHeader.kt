package vlad.dima.sales.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.model.User
import java.lang.Math.min

@Composable
fun UserHeader(
    user: User
) {
    val localContext = LocalContext.current
    val initials = remember(user) {
        user.fullName
            .split(' ')
            .let {
                it.slice(0 until 2.coerceAtMost(it.size))
            }
            .map { it[0] }
            .joinToString(separator = "")
    }
    val initialsGradient =
        listOf(
            MaterialTheme.colors.primaryVariant,
            MaterialTheme.colors.secondaryVariant
        )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.surface,
                        MaterialTheme.colors.background
                    )
                )
            )
            .systemBarsPadding()
            .padding(8.dp),
    ) {
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp),
            onClick = { (localContext as Activity).finish() }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.GoBack),
                tint = MaterialTheme.colors.onSurface
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(initialsGradient))
                    .size(100.dp)
                    .padding(8.dp)
            ) {
                Text(
                    text = initials,
                    modifier = Modifier
                        .align(Alignment.Center),
                    fontSize = 48.sp,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = user.fullName, color = MaterialTheme.colors.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = stringResource(id = R.string.Email),
                    tint = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = user.email, color = MaterialTheme.colors.onSurface)
            }
        }
    }
}