package vlad.dima.sales.ui.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.SignalWifiBad
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun AsyncImage(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    shapeCrop: Shape? = null,
    size: Dp? = null,
    contentDescription: String,
    contentScale: ContentScale,
    loading: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    var isError by rememberSaveable {
        mutableStateOf(false)
    }
    if (imageUri != Uri.EMPTY) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            loading = {
                loading()
            },
            modifier = modifier
                .let {
                    var modifier1 = it
                    if (shapeCrop != null) {
                        modifier1 = modifier1.clip(shapeCrop)
                    }
                    if (onClick != null && !isError) {
                        modifier1 = modifier1.clickable(onClick = onClick)
                    }
                    if (size != null) {
                        modifier1 = modifier1.size(size)
                    }
                    modifier1
                },
            error = {
                Box(
                    modifier = modifier
                        .let {
                            var modifier1 = it
                            if (shapeCrop != null) {
                                modifier1 = modifier1.clip(shapeCrop)
                            }
                            if (size != null) {
                                modifier1 = modifier1.size(size)
                            }
                            modifier1
                        }
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SignalWifiBad,
                        contentDescription = contentDescription,
                        modifier = modifier
                            .align(Alignment.Center)
                            .let {
                                if (size != null) {
                                    return@let it.size(size / 2)
                                }
                                it
                            },
                        tint = Color.Black
                    )
                }
            },
            onError = {
                isError = true
            },
            onSuccess = {
                isError = false
            }
        )
    } else {
        Box(
            modifier = modifier
                .let {
                    var modifier1 = it
                    if (shapeCrop != null) {
                        modifier1 = modifier1.clip(shapeCrop)
                    }
                    if (size != null) {
                        modifier1 = modifier1.size(size)
                    }
                    modifier1
                }
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Filled.NoPhotography,
                contentDescription = contentDescription,
                modifier = modifier
                    .align(Alignment.Center)
                    .let {
                         if (size != null) {
                             return@let it.size(size / 2)
                         }
                        it
                    },
                tint = Color.Black
            )
        }
    }
}