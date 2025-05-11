package com.example.geminiapikey

import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add this flag to make the window content secure
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val fileName = intent.getStringExtra("fileName")


        setContent {
            Scaffold { paddingValues ->
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Inject JavaScript to disable selection and context menu
                                    view?.loadUrl(
                                        "javascript:(function() {" +
                                                "document.documentElement.style.webkitUserSelect='none';" +
                                                "document.documentElement.style.userSelect='none';" +
                                                "document.oncontextmenu = function() { return false; };" +
                                                "})()"
                                    )
                                }
                            }
                            settings.javaScriptEnabled = true
                            loadUrl("file:///android_asset/$fileName")
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}
