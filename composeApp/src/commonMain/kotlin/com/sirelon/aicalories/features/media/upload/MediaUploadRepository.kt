package com.sirelon.aicalories.features.media.upload

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.readByteArray
import com.sirelon.aicalories.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.uuid.Uuid

class MediaUploadRepository(
    private val client: SupabaseClient,
) {

    fun uploadFile(platformContext: PlatformContext, file: KmpFile): Flow<UploadStatus> = flow {
        emitAll(
            client.uploadFile(
                path = file.getName(platformContext)
                    ?: file.getPath(platformContext)
                    ?: Uuid.random().toString(),
                byteArray = file.readByteArray(platformContext),
            ),
        )
    }
}
