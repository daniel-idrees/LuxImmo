package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePainter.State.Loading
import coil3.compose.rememberAsyncImagePainter
import com.example.core.ui.R

@Composable
fun DynamicAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: Painter = painterResource(R.drawable.ic_placeholder),
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {

        var isLoading by remember { mutableStateOf(true) }
        var isError by remember { mutableStateOf(false) }

        val imageLoader = rememberAsyncImagePainter(
            model = imageUrl,
            onState = { state ->
                isLoading = state is Loading
                isError = state is AsyncImagePainter.State.Error
            },
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                color = MaterialTheme.colorScheme.surface,
            )
        } else {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = if (isError.not()) imageLoader else placeholder,
                contentDescription = contentDescription,
                contentScale = contentScale,
            )
        }
    }
}