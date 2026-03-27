package com.sirelon.aicalories.features.seller

import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.add_photo
import com.sirelon.aicalories.composeapp.generated.resources.ic_snap_logo
import com.sirelon.aicalories.composeapp.generated.resources.photos_count_limit
import com.sirelon.aicalories.composeapp.generated.resources.sell_snap
import com.sirelon.aicalories.composeapp.generated.resources.snap_photo_ad_desc
import com.sirelon.aicalories.composeapp.generated.resources.take_photo
import com.sirelon.aicalories.composeapp.generated.resources.tip_angles
import com.sirelon.aicalories.composeapp.generated.resources.tip_defects
import com.sirelon.aicalories.composeapp.generated.resources.tip_lighting
import com.sirelon.aicalories.composeapp.generated.resources.tips_for_better_photos
import com.sirelon.aicalories.composeapp.generated.resources.turn_stuff_into_olx_listings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SellerScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val platformContext = LocalPlatformContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionController = rememberPermissionController(permission = Permission.Camera)

    val photoPicker = rememberPhotoPickerController(
        permissionController = permissionController,
        onResult = {
            // TODO: Handle photo result
        },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SellerHeader()
            AddPhotoSection(
                onTakePhotoClick = {
                    photoPicker.captureWithCamera()
                },
                onUploadClick = {
                    photoPicker.pickFromGallery()
                }
            )
            TipsSection()
        }
    }
}

@Composable
private fun SellerHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF28B44),
                        Color(0xFFE67E22)
                    )
                )
            )
            .padding(24.dp)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-50).dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(70.dp))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50.dp))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_snap_logo),
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = Color(0xFFE67E22)
                    )
                }
                Text(
                    text = stringResource(Res.string.sell_snap),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(Res.string.turn_stuff_into_olx_listings),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp
            )

            Text(
                text = stringResource(Res.string.snap_photo_ad_desc),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AddPhotoSection(
    onTakePhotoClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoCount: Int = 0,
    maxPhotos: Int = 5
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.add_photo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2933)
                )
                Text(
                    text = stringResource(Res.string.photos_count_limit, photoCount, maxPhotos),
                    fontSize = 15.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTakePhotoClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE67E22)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(Res.string.take_photo),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                FilledIconButton(
                    onClick = onUploadClick,
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF475569)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsSection(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0F4FF)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFDCE4FF)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = Color(0xFF4F46E5)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(Res.string.tips_for_better_photos),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2933)
                )

                TipItem(text = stringResource(Res.string.tip_lighting))
                TipItem(text = stringResource(Res.string.tip_angles))
                TipItem(text = stringResource(Res.string.tip_defects))
            }
        }
    }
}

@Composable
private fun TipItem(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color(0xFF10B981)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun SellerScreenPreview() {
    AppTheme {
        SellerScreen()
    }
}
