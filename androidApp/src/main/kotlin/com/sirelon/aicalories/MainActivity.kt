package com.sirelon.aicalories

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sirelon.aicalories.features.seller.auth.data.OlxAuthCallbackBridge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
            ?.takeIf { it.startsWith("aicalories://olx-auth") }
            ?.let(OlxAuthCallbackBridge::publishCallback)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
