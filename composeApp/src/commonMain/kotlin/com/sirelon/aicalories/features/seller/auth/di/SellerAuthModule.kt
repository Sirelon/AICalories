package com.sirelon.aicalories.features.seller.auth.di

import com.sirelon.aicalories.features.seller.auth.data.BuildConfigOlxCredentialsProvider
import com.sirelon.aicalories.features.seller.auth.data.DefaultOlxRedirectHandler
import com.sirelon.aicalories.features.seller.auth.data.OlxApiClient
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthRepository
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthSessionStore
import com.sirelon.aicalories.features.seller.auth.data.OlxCredentialsProvider
import com.sirelon.aicalories.features.seller.auth.data.OlxRedirectHandler
import com.sirelon.aicalories.features.seller.auth.data.OlxTokenStore
import com.sirelon.aicalories.features.seller.auth.data.createOlxAuthorizedHttpClient
import com.sirelon.aicalories.features.seller.auth.data.createOlxHttpClient
import com.sirelon.aicalories.features.seller.auth.presentation.SellerAuthViewModel
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
    single { OlxTokenStore() }
    single { OlxAuthSessionStore() }
    single { DefaultOlxRedirectHandler() } bind OlxRedirectHandler::class
    single { OlxAuthRepository(get(olxHttpClientQualifier), get(), get(), get(), get()) }
    single { OlxApiClient(get(olxAuthorizedHttpClientQualifier)) }
    viewModelOf(::SellerAuthViewModel)
}
