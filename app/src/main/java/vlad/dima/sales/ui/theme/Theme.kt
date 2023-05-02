package vlad.dima.sales.ui.theme

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import vlad.dima.sales.R

private val DarkColorPalette = darkColors(
    primary = GreenPrimary,
    primaryVariant = GreenPrimaryLight,
    onPrimary = Color.White,
    secondary = TealSecondary,
    secondaryVariant = TealSecondaryLight,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = GreenPrimary,
    primaryVariant = GreenPrimaryDark,
    secondary = TealSecondary,
    secondaryVariant = TealSecondaryDark,
    surface = LightSurface,
    background = LightBackground
)

val RobotoFont = FontFamily(
    Font(R.font.roboto_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.roboto_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

val Typography.italicText: TextStyle
    @Composable
    get() {
        return TextStyle(
            fontFamily = RobotoFont,
            fontWeight = FontWeight.Normal
        )
    }

@Composable
fun SalesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    defaultSystemBarsColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    if (defaultSystemBarsColor) {
        val systemUiController = rememberSystemUiController()
        systemUiController.let {
            it.setStatusBarColor(
                color = if (darkTheme) GreenPrimaryDark else GreenPrimary
            )
            it.setNavigationBarColor(
                color = if (darkTheme) DarkSurface else LightSurface
            )
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}