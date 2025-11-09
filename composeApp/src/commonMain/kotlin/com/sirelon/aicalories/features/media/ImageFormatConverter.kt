package com.sirelon.aicalories.features.media

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile

interface ImageFormatConverter {
    suspend fun convert(
        platformContext: PlatformContext,
        file: KmpFile,
    ): KmpFile
}

class PassthroughImageFormatConverter : ImageFormatConverter {
    override suspend fun convert(
        platformContext: PlatformContext,
        file: KmpFile,
    ): KmpFile = file
}

expect fun imageFormatConverter(): ImageFormatConverter
