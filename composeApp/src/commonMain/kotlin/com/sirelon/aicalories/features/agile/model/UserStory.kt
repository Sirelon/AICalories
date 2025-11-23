package com.sirelon.aicalories.features.agile.model

data class UserStory(
    val id: Int,
    val name: String,
    val tickets: List<Ticket>,
)