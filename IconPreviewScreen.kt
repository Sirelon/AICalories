package com.sirelon.aicalories.designsystem.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sirelon.aicalories.composeapp.generated.resources.Res
import com.sirelon.aicalories.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class IconItem(val name: String, val resource: DrawableResource)

@Composable
fun IconPreviewScreen() {
    val icons = listOf(
        IconItem("ArrowLeft", Res.drawable.ArrowLeft),
        IconItem("Baby", Res.drawable.Baby),
        IconItem("BookOpen", Res.drawable.BookOpen),
        IconItem("Camera", Res.drawable.Camera),
        IconItem("Car", Res.drawable.Car),
        IconItem("Check", Res.drawable.Check),
        IconItem("ChevronLeft", Res.drawable.ChevronLeft),
        IconItem("ChevronRight", Res.drawable.ChevronRight),
        IconItem("CircleAlert", Res.drawable.CircleAlert),
        IconItem("CircleCheckBig", Res.drawable.CircleCheckBig),
        IconItem("Copy", Res.drawable.Copy),
        IconItem("DollarSign", Res.drawable.DollarSign),
        IconItem("Dumbbell", Res.drawable.Dumbbell),
        IconItem("Eye", Res.drawable.Eye),
        IconItem("FileText", Res.drawable.FileText),
        IconItem("Frown", Res.drawable.Frown),
        IconItem("Gift", Res.drawable.Gift),
        IconItem("Heart", Res.drawable.Heart),
        IconItem("Home", Res.drawable.Home),
        IconItem("LayoutGrid", Res.drawable.LayoutGrid),
        IconItem("Meh", Res.drawable.Meh),
        IconItem("Palette", Res.drawable.Palette),
        IconItem("PenLine", Res.drawable.PenLine),
        IconItem("RefreshCw", Res.drawable.RefreshCw),
        IconItem("Server", Res.drawable.Server),
        IconItem("Share2", Res.drawable.Share2),
        IconItem("Shirt", Res.drawable.Shirt),
        IconItem("Smartphone", Res.drawable.Smartphone),
        IconItem("Sparkles", Res.drawable.Sparkles),
        IconItem("Tag", Res.drawable.Tag),
        IconItem("TreePine", Res.drawable.TreePine),
        IconItem("TrendingUp", Res.drawable.TrendingUp),
        IconItem("TriangleAlert", Res.drawable.TriangleAlert),
        IconItem("Upload", Res.drawable.Upload),
        IconItem("User", Res.drawable.User),
        IconItem("WandSparkles", Res.drawable.WandSparkles),
        IconItem("WifiOff", Res.drawable.WifiOff),
        IconItem("Wrench", Res.drawable.Wrench),
        IconItem("X", Res.drawable.X),
    )

    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Icon Preview (${icons.size} icons)",
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 20.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier.fillMaxSize(),
            content = {
                items(icons) { icon ->
                    IconPreviewItem(icon)
                }
            }
        )
    }
}

@Composable
private fun IconPreviewItem(icon: IconItem) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.LightGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(icon.resource),
                contentDescription = icon.name,
                modifier = Modifier.padding(bottom = 8.dp),
                tint = Color.Black
            )
            Text(
                text = icon.name,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun IconPreviewScreenPreview() {
    IconPreviewScreen()
}
