package com.sirelon.aicalories.features.attributes.di

import com.sirelon.aicalories.features.attributes.data.AttributeMapper
import com.sirelon.aicalories.features.attributes.data.OlxAttributesRepository
import com.sirelon.aicalories.features.attributes.domain.AttributeValidator
import com.sirelon.aicalories.features.attributes.presentation.AttributesViewModel
import com.sirelon.aicalories.features.seller.auth.di.olxAuthorizedHttpClientQualifier
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val attributesModule = module {
    single { AttributeMapper() }
    single { AttributeValidator() }
    single { OlxAttributesRepository(get(olxAuthorizedHttpClientQualifier), get()) }
    viewModelOf(::AttributesViewModel)
}
