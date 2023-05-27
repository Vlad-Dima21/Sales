package vlad.dima.sales.ui.dashboard.salesman_dashboard.unassigned

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vlad.dima.sales.R
import vlad.dima.sales.model.User
import vlad.dima.sales.ui.theme.GreenPrimaryLight

@OptIn(ExperimentalTextApi::class)
@Composable
fun UnassignedUserPage(
    currentUser: User,
    onLogout: () -> Unit
) {
    val firstGradientColor = MaterialTheme.colors.primaryVariant
    val secondGradientColor = MaterialTheme.colors.secondaryVariant
    val firstName = remember(currentUser) {
        with(currentUser) {
            if (fullName.contains(" ")) {
                return@with fullName.split(" ")[0]
            }
            return@with fullName
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            Modifier
                .align(Center)
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    text = stringResource(id = R.string.Welcome),
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$firstName,",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(
                                firstGradientColor,
                                secondGradientColor
                            )
                        )
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.UnassignedDescription),
                fontSize = 16.sp,
                color = MaterialTheme.colors.onBackground.copy(.6f)
            )

            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.align(CenterHorizontally),
                shape = CircleShape,
                border = if (!MaterialTheme.colors.isLight) BorderStroke(3.dp, MaterialTheme.colors.onBackground) else null
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.LogOut),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun UnassignedPreview() {
    UnassignedUserPage(currentUser = User("Gheorghe Laurențiu")) {}
}