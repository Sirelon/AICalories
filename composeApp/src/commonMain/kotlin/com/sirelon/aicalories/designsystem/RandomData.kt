package com.sirelon.aicalories.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import kotlin.random.Random

object RandomData {

    fun randomChip(label: String? = null): ChipData {
        val foodTags = listOf(
            "Protein", "Carbs", "Fats", "Fiber", "Vitamins", "Low Sugar",
            "High Protein", "Keto", "Vegan", "Gluten-Free", "Breakfast", "Snack",
            "Post-Workout", "Hydration", "Balanced", "Low Calorie"
        )

        val icons = listOf(
            Icons.Default.FitnessCenter,   // for protein, workouts
            Icons.Default.EnergySavingsLeaf, // for healthy/green
            Icons.Default.LocalDining,     // for general food
            Icons.Default.WaterDrop,       // for hydration
            Icons.Default.Favorite,        // for heart/healthy
            Icons.Default.Whatshot,        // for energy/fat burn
            Icons.Default.Eco,             // for natural/vegan
            null
        )

        val text = label ?: foodTags.random()
        val icon = icons.random()
        return ChipData(text = text, icon = icon)
    }


    fun randomQualityChip(): ChipData {
        val tags = listOf(
            "High quality",
            "Needs attention",
            "Balanced",
            "Uncertain",
            "Too fatty",
            "Too sweet",
            "Protein-rich",
            "Low energy"
        )

        val icons = listOf(
            Icons.Default.Star,           // quality
            Icons.Default.Warning,        // needs attention
            Icons.Default.Balance,        // balanced
            Icons.Default.Whatshot,       // too fatty
            Icons.Default.FavoriteBorder, // too sweet
            Icons.Default.FitnessCenter,  // protein
            Icons.Default.EnergySavingsLeaf, // low energy
            null
        )

        val index = tags.indices.random()
        return ChipData(text = tags[index], icon = icons.getOrNull(index))
    }

    fun randomInsightChip(random: Random, average: Int): List<ChipData> {

        val options = listOf(
            ChipData("Avg ${average} kcal/day", Icons.Default.LocalFireDepartment),
            ChipData("Consistency +${random.nextInt(2, 12)}%", Icons.Default.Insights),
            ChipData("Protein +${random.nextInt(5, 20)}g", Icons.Default.FitnessCenter),
            ChipData("Hydration ${random.nextInt(1, 4)}L", Icons.Default.WaterDrop),
            ChipData("Sleep ${random.nextInt(6, 9)}h", Icons.Default.Bedtime),
            ChipData("Steps ${random.nextInt(6000, 12000)}", Icons.Default.DirectionsRun),
            ChipData("Fat −${random.nextInt(5, 15)}%", Icons.Default.Whatshot),
            ChipData("Carbs ${random.nextInt(40, 65)}%", Icons.Default.LocalDining),
        )

        return options.shuffled(random).take(2)
    }
}