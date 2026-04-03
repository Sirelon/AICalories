package com.sirelon.aicalories.datastore

import java.io.File

actual fun createKeyValueStore(name: String): KeyValueStore = createDataStoreKeyValueStore {
    val appDataDir = File(System.getProperty("user.home"), ".aicalories/datastore")
    appDataDir.mkdirs()
    File(appDataDir, "$name.preferences_pb").absolutePath
}
