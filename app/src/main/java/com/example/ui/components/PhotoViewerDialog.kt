package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.ItemPhoto
import java.io.File

@Composable
fun PhotoViewerDialog(
    photo: ItemPhoto,
    onDismiss: () -> Unit,
    onSetPrimary: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("photo_viewer_dialog")
        ) {
            // Fullscreen photo
            AsyncImage(
                model = File(photo.photoPath),
                contentDescription = "Photo en plein écran",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top bar controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = CircleShape,
                    modifier = Modifier.testTag("close_photo_viewer")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onSetPrimary != null) {
                        FilledIconButton(
                            onClick = onSetPrimary,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (photo.isPrimary) Color(0xFFF59E0B) else Color.Black.copy(alpha = 0.5f)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.testTag("set_primary_photo")
                        ) {
                            Icon(
                                imageVector = if (photo.isPrimary) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (photo.isPrimary) "Photo principale" else "Définir comme principale",
                                tint = Color.White
                            )
                        }
                    }

                    if (onDelete != null) {
                        FilledIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                            shape = CircleShape,
                            modifier = Modifier.testTag("delete_photo_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer photo", tint = Color.White)
                        }
                    }
                }
            }

            if (photo.isPrimary) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "★ Photo principale",
                        color = Color(0xFFFCD34D),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
