package com.sirelon.aicalories.features.agile.model

import com.sirelon.aicalories.features.agile.Estimation

data class Ticket(
    val id: Int,
    val name: String,
    val estimation: Estimation = Estimation.M,
)
