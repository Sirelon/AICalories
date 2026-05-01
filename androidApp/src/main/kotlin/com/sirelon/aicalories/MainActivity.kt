package com.sirelon.aicalories

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sirelon.aicalories.datastore.initAndroidKeyValueStore
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthCallbackBridge
import com.sirelon.aicalories.platform.initAndroidUrlOpener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        initAndroidKeyValueStore(filesDir.absolutePath)
        initAndroidUrlOpener(this)
        publishOlxCallback(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        publishOlxCallback(intent)
    }

    private fun publishOlxCallback(intent: Intent?) {
        intent?.dataString
            ?.takeIf { it.startsWith("selolxai://olx-auth") }
            ?.let(OlxAuthCallbackBridge::publishCallback)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
