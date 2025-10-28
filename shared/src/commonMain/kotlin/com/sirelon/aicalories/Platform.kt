package com.sirelon.aicalories

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform