package com.example.businesscardapp.ui.component

import android.net.Uri
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage  // Coil 사용 시


@Composable
fun ZoomableImage(uri: Uri) {
    var scale by remember { mutableStateOf(1f) }
    val state = rememberTransformableState { zoomChange, _, _ ->
        scale *= zoomChange
    }

    Box(
        modifier = Modifier
            .size(220.dp)
            .padding(8.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .transformable(state = state)
    ) {
        AsyncImage(model = uri, contentDescription = null)
    }
}
