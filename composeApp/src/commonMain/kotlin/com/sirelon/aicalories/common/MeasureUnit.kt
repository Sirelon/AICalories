package com.sirelon.aicalories.common

enum class MeasureUnit {
    Grams, Kcal;
}

fun MeasureUnit.fullName() = when (this) {
    MeasureUnit.Grams -> "grams"
    MeasureUnit.Kcal -> "kCalories"
}

fun MeasureUnit.shortName() = when (this) {
    MeasureUnit.Grams -> "g"
    MeasureUnit.Kcal -> "kcal"
}