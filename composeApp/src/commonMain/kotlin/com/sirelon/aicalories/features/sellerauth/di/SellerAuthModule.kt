package com.sirelon.aicalories.features.sellerauth.di

import com.sirelon.aicalories.features.sellerauth.data.BuildConfigOlxCredentialsProvider
import com.sirelon.aicalories.features.sellerauth.data.DefaultOlxRedirectHandler
import com.sirelon.aicalories.features.sellerauth.data.InMemoryOlxAuthSessionStore
import com.sirelon.aicalories.features.sellerauth.data.InMemoryOlxTokenStore
import com.sirelon.aicalories.features.sellerauth.data.OlxApiClient
import com.sirelon.aicalories.features.sellerauth.data.OlxAuthRepository
import com.sirelon.aicalories.features.sellerauth.data.OlxAuthSessionStore
import com.sirelon.aicalories.features.sellerauth.data.OlxCredentialsProvider
import com.sirelon.aicalories.features.sellerauth.data.OlxRedirectHandler
import com.sirelon.aicalories.features.sellerauth.data.OlxTokenStore
import com.sirelon.aicalories.features.sellerauth.data.createOlxAuthorizedHttpClient
import com.sirelon.aicalories.features.sellerauth.data.createOlxHttpClient
import com.sirelon.aicalories.features.sellerauth.presentation.SellerAuthViewModel
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val olxHttpClientQualifier = named("olxHttpClient")
val olxAuthorizedHttpClientQualifier = named("olxAuthorizedHttpClient")

val sellerAuthModule = module {
    single<HttpClient>(qualifier = olxHttpClientQualifier) { createOlxHttpClient() }
    single<HttpClient>(qualifier = olxAuthorizedHttpClientQualifier) {
        createOlxAuthorizedHttpClient(
            authRefreshClient = get(olxHttpClientQualifier),
            credentialsProvider = get(),
            tokenStore = get(),
        )
    }
    single { BuildConfigOlxCredentialsProvider() } bind OlxCredentialsProvider::class
    single { InMemoryOlxTokenStore() } bind OlxTokenStore::class
    single { InMemoryOlxAuthSessionStore() } bind OlxAuthSessionStore::class
    single { DefaultOlxRedirectHandler() } bind OlxRedirectHandler::class
    single { OlxAuthRepository(get(olxHttpClientQualifier), get(), get(), get(), get()) }
    single { OlxApiClient(get(olxAuthorizedHttpClientQualifier)) }
    viewModelOf(::SellerAuthViewModel)
}
