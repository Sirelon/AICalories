package com.sirelon.aicalories.di

import com.sirelon.aicalories.Greeting
import org.koin.dsl.module

val appModule = module {
    single { Greeting() }
}
