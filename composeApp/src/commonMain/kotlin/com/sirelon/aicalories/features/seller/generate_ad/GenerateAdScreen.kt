package com.sirelon.aicalories.features.seller.generate_ad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.mohamedrejeb.calf.permissions.Camera
import com.mohamedrejeb.calf.permissions.Permission
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.generate_ad_with_ai
import com.sirelon.aicalories.composeapp.generated.resources.ic_snap_logo
import com.sirelon.aicalories.composeapp.generated.resources.sell_snap
import com.sirelon.aicalories.composeapp.generated.resources.snap_photo_ad_desc
import com.sirelon.aicalories.composeapp.generated.resources.tip_angles
import com.sirelon.aicalories.composeapp.generated.resources.tip_defects
import com.sirelon.aicalories.composeapp.generated.resources.tip_lighting
import com.sirelon.aicalories.composeapp.generated.resources.tips_for_better_photos
import com.sirelon.aicalories.composeapp.generated.resources.turn_stuff_into_olx_listings
import com.sirelon.aicalories.designsystem.AppDimens
import com.sirelon.aicalories.designsystem.AppTheme
import com.sirelon.aicalories.designsystem.IconWithBackground
import com.sirelon.aicalories.designsystem.buttons.AppButton
import com.sirelon.aicalories.designsystem.buttons.AppButtonDefaults
import com.sirelon.aicalories.features.media.PermissionDialogs
import com.sirelon.aicalories.features.media.rememberPermissionController
import com.sirelon.aicalories.features.media.rememberPhotoPickerController
import com.sirelon.aicalories.features.media.ui.PhotosSection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenerateAdScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppButton(
                modifier = Modifier.fillMaxWidth(),
                style = AppButtonDefaults.primary(),
                text = stringResource(Res.string.generate_ad_with_ai),
                onClick = { },
                icon = Icons.Rounded.Star,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(AppDimens.Spacing.xl3)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            SellerHeader()
            PhotosSection(
                onTakePhotoClick = photoPicker::captureWithCamera,
                onUploadClick = photoPicker::pickFromGallery,
                // TODO:
                files = emptyMap(),
            )
            TipsSection()
        }
    }

    PermissionDialogs(
        controller = permissionController,
    )
}

@Composable
private fun SellerHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.BorderRadius.xl11))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppTheme.colors.warningVariant,
                        AppTheme.colors.warning,
                    )
                )
            )
            .padding(AppDimens.Spacing.xl6)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(AppDimens.Size.xl21 + AppDimens.Size.xl)
                .align(Alignment.TopEnd)
                .offset(
                    x = AppDimens.Spacing.xl10,
                    y = -(AppDimens.Size.xl8 + AppDimens.Size.xl4),
                )
                .background(AppTheme.colors.onPrimary.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(AppDimens.Size.xl14 + AppDimens.Size.xl9)
                .align(Alignment.BottomStart)
                .offset(
                    x = -(AppDimens.Spacing.xl5 + AppDimens.Spacing.l),
                    y = AppDimens.Spacing.xl5 + AppDimens.Spacing.l,
                )
                .background(AppTheme.colors.onPrimary.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl5)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl)
            ) {
                IconWithBackground(
                    modifier = Modifier.size(AppDimens.Size.xl11),
                    backgroundColor = AppTheme.colors.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_snap_logo),
                        contentDescription = null,
                        tint = AppTheme.colors.warning,
                    )
                }
                Text(
                    text = stringResource(Res.string.sell_snap),
                    color = AppTheme.colors.onPrimary,
                    fontSize = AppDimens.TextSize.xl6,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(Res.string.turn_stuff_into_olx_listings),
                color = AppTheme.colors.onPrimary,
                fontSize = AppDimens.TextSize.xl7,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = AppDimens.TextSize.xl8
            )

            Text(
                text = stringResource(Res.string.snap_photo_ad_desc),
                color = AppTheme.colors.onPrimary.copy(alpha = 0.9f),
                fontSize = AppDimens.TextSize.xl3,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TipsSection(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.BorderRadius.xl7),
        color = AppTheme.colors.infoSurface
    ) {
        Row(
            modifier = Modifier.padding(AppDimens.Spacing.xl5),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.xl3)
        ) {
            IconWithBackground(
                modifier = Modifier.size(AppDimens.Size.xl11),
                backgroundColor = AppTheme.colors.infoSurfaceVariant,
                shape = RoundedCornerShape(AppDimens.BorderRadius.l),
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = AppTheme.colors.primary
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.Spacing.l)
            ) {
                Text(
                    text = stringResource(Res.string.tips_for_better_photos),
                    fontSize = AppDimens.TextSize.xl4,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
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
        horizontalArrangement = Arrangement.spacedBy(AppDimens.Spacing.m)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(AppDimens.Size.xl4),
            tint = AppTheme.colors.success
        )
        Text(
            text = text,
            fontSize = AppDimens.TextSize.xl2,
            color = AppTheme.colors.onSurfaceMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun GenerateAdScreenPreview() {
    AppTheme {
        GenerateAdScreen()
    }
}
