package com.sirelon.aicalories.features.media.di

import com.sirelon.aicalories.features.media.imageFormatConverter
import com.sirelon.aicalories.features.media.upload.MediaUploadHelper
import com.sirelon.aicalories.features.media.upload.MediaUploadRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val mediaModule = module {
    single { imageFormatConverter() }
    singleOf(::MediaUploadRepository)
    singleOf(::MediaUploadHelper)
}
