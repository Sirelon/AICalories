package com.sirelon.aicalories.features.history.presentation

import com.sirelon.aicalories.designsystem.ChipStyle
import com.sirelon.aicalories.designsystem.RandomData
import com.sirelon.aicalories.features.history.ui.CaloriePointRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryAttachmentRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryEntryRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryFoodRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryGroupRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryReportSummaryRenderModel
import com.sirelon.aicalories.features.history.ui.HistoryScreenRenderModel
import com.sirelon.aicalories.features.history.ui.MacroBreakdownRenderModel
import com.sirelon.aicalories.features.history.ui.WeeklyCaloriesRenderModel
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock

object HistorySampleDataProvider {

    private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val mealNames = listOf(
        "Grilled Salmon",
        "Avocado Toast",
        "Protein Oats",
        "Veggie Bowl",
        "Chicken Stir Fry",
        "Pesto Pasta",
        "Rainbow Salad",
        "Tofu Scramble",
        "Berry Smoothie",
    )
    private val ingredientSuffixes = listOf(
        "with quinoa",
        "and roasted veggies",
        "served with greens",
        "topped with seeds",
        "with citrus dressing",
        "and avocado slices",
        "with brown rice",
    )
    private val sampleImages = listOf(
        "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?w=400",
        "https://images.unsplash.com/photo-1484981138541-3d074aa97716?w=400",
        "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400",
        "https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400",
    )

    fun randomRenderModel(
        seed: Long = Clock.System.now().toEpochMilliseconds()
    ): HistoryScreenRenderModel {
        val random = Random(seed)
        val weeklyPoints = dayLabels.map { day ->
            val calories = random.nextInt(1700, 2500)
            CaloriePointRenderModel(
                id = day.lowercase(),
                dayLabel = day,
                caloriesValue = calories,
                caloriesLabel = "${(calories / 100f).roundToInt() / 10.0}k",
            )
        }
        val total = weeklyPoints.sumOf { it.caloriesValue }
        val average = total / weeklyPoints.size

        val groups = (0 until random.nextInt(2, 4)).map { dayOffset ->
            val dateLabel = "Nov ${2 - dayOffset}, 2025"
            HistoryGroupRenderModel(
                groupId = "group-$dayOffset",
                dayLabel = dateLabel,
                entries = (0 until random.nextInt(1, 3)).map { entryIndex ->
                    randomEntry(
                        random = random,
                        id = (dayOffset * 10 + entryIndex + 1).toLong(),
                        dateLabel = dateLabel,
                    )
                },
            )
        }

        return HistoryScreenRenderModel(
            insights = RandomData.randomInsightChip(random, average),

            weeklySummary = WeeklyCaloriesRenderModel(
                title = "Calories this week",
                totalLabel = "${total} kcal",
                changeLabel = "${if (random.nextBoolean()) "+" else "-"}${
                    random.nextInt(
                        1,
                        6
                    )
                }% vs last week",
                points = weeklyPoints,
                targetLabel = "Target ${average - 150} kcal",
            ),
            groupedEntries = groups,
            highlightedEntryId = groups.firstOrNull()?.entries?.firstOrNull()?.id,
        )
    }

    private fun randomEntry(
        random: Random,
        id: Long,
        dateLabel: String,
    ): HistoryEntryRenderModel {
        val calories = random.nextInt(350, 820)
        val foods = (0 until random.nextInt(2, 4)).map { index ->
            HistoryFoodRenderModel(
                id = "$id-food-$index",
                title = mealNames.random(random),
                description = ingredientSuffixes.random(random),
                quantityLabel = "${random.nextInt(80, 240)} g",
                caloriesLabel = "${random.nextInt(120, 320)} kcal",
                macroLabel = "P${random.nextInt(8, 32)} • F${
                    random.nextInt(
                        5,
                        20
                    )
                } • C${random.nextInt(20, 60)}",
                confidenceLabel = "Confidence ${random.nextInt(80, 97)}%",
                fromImage = random.nextBoolean(),
                fromNote = random.nextBoolean(),
            )
        }
        val macros = MacroBreakdownRenderModel(
            calories = "$calories kcal",
            protein = "${random.nextInt(20, 60)} g",
            fat = "${random.nextInt(10, 35)} g",
            carbs = "${random.nextInt(30, 90)} g",
        )
        val attachments =
            sampleImages.shuffled(random).take(random.nextInt(1, 3)).mapIndexed { index, url ->
                HistoryAttachmentRenderModel(
                    id = "$id-attachment-$index",
                    previewUrl = url,
                    description = "Meal photo ${index + 1}",
                )
            }
        val summary = HistoryReportSummaryRenderModel(
            advice = "Add leafy greens for extra fiber and micronutrients.",
            qualityLabel = RandomData.randomQualityChip(),
            issues = if (random.nextBoolean()) {
                listOf(RandomData.randomChip(style = ChipStyle.Error))
            } else {
                emptyList()
            },
            uncertainties = if (random.nextBoolean()) {
                listOf(RandomData.randomChip())
            } else {
                emptyList()
            },
            checklist = listOf(
                RandomData.randomChip(style = ChipStyle.Success),
                RandomData.randomChip(style = ChipStyle.Success),
            ),
        )

        return HistoryEntryRenderModel(
            id = id,
            dateLabel = dateLabel,
            timeLabel = "${random.nextInt(6, 22)}:${
                random.nextInt(0, 59).toString().padStart(2, '0')
            } ${
                if (random.nextBoolean()) "AM" else "PM"
            }",
            caloriesLabel = "$calories kcal",
            note = listOf(
                "Post workout meal with healthy fats.",
                "Quick lunch with whole grains.",
                "Dinner with extra veggies.",
                "Protein focused breakfast.",
            ).random(random),
            attachments = attachments,
            foods = foods,
            macros = macros,
            summary = summary,
            tags = listOf("AI sourced", "Manual note", "Contains nuts").shuffled(random)
                .take(random.nextInt(0, 3)).map { RandomData.randomChip(it) },
            photoCountLabel = "${attachments.size} photo${if (attachments.size > 1) "s" else ""}",
            confidenceLabel = "Confidence ${random.nextInt(80, 98)}%",
        )
    }
}
