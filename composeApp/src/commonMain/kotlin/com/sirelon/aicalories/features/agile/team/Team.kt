package com.sirelon.aicalories.features.agile.team

/**
 * Represents a delivery team and its capacity.
 */
data class Team(
    val id: Int,
    val name: String,
    val peopleCount: Int,
    val capacity: Int,
)
