package com.landside.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.coroutines.launch
import java.io.File

class WebActivity:AppCompatActivity(R.layout.activity_web) {
    private val REQUEST_ALBUM = 10003
    var fileSelectCallbk: ValueCallback<Array<Uri>>? = null

    val webViewClient = object : WebViewClient() {

        override fun onPageFinished(
            view: android.webkit.WebView,
            url: String
        ) {
        }

        override fun onReceivedSslError(
            view: android.webkit.WebView,
            handler: SslErrorHandler,
            error: SslError
        ) {
            handler.proceed() // 接受网站证书
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ws = web_view.settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        ws.builtInZoomControls = false
        ws.loadWithOverviewMode = true
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.databaseEnabled = true
        ws.setSupportMultipleWindows(true)// 新加
        ws.javaScriptCanOpenWindowsAutomatically = true
        web_view.requestFocus()
        ws.allowFileAccess = true
        ws.allowContentAccess = true
        ws.allowFileAccessFromFileURLs = true
        ws.allowUniversalAccessFromFileURLs = true
        ws.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        ws.setSupportMultipleWindows(false)
        ws.pluginState = WebSettings.PluginState.ON_DEMAND
        ws.setSupportZoom(true)
        ws.useWideViewPort = true
        ws.builtInZoomControls = false // 支持页面放大缩小按钮
        ws.savePassword = true
        CookieManager.getInstance()
            .setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance()
                .setAcceptThirdPartyCookies(web_view, true)
        }
        web_view.scrollBarStyle = android.webkit.WebView.SCROLLBARS_OUTSIDE_OVERLAY
        // 防范安全漏洞
        web_view.removeJavascriptInterface("searchBoxJavaBridge_")
        web_view.removeJavascriptInterface("accessibility")
        web_view.removeJavascriptInterface("accessibilityTraversal")
        web_view.setDownloadListener { url, _, _, _, _ ->
            try {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (ignored: Exception) {
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            android.webkit.WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        }
        web_view.webViewClient = webViewClient

        web_view.webChromeClient = object : WebChromeClient() {
            var mCallback: CustomViewCallback? = null

            override fun onProgressChanged(
                view: android.webkit.WebView,
                newProgress: Int
            ) {
                if (newProgress == 100) {
                    progress.visibility = View.GONE
                } else {
                    progress.visibility = View.VISIBLE
                    progress.progress = newProgress
                }
            }

            override fun onCreateWindow(
                view: android.webkit.WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@WebActivity)
                newWebView.webViewClient = webViewClient
                view.addView(newWebView)
                val transport: android.webkit.WebView.WebViewTransport =
                    resultMsg.obj as android.webkit.WebView.WebViewTransport
                //以下的操作应该就是让新的webview去加载对应的url等操作。
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onReceivedTitle(
                view: android.webkit.WebView?,
                title: String?
            ) {
                super.onReceivedTitle(view, title)
                if (title?.contains("-h5") == false) {
                    this@WebActivity.title = title ?: ""
                }
            }

            override fun onShowFileChooser(
                webView: android.webkit.WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                RxPermissions(this@WebActivity)
                    .request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    .subscribe { granted ->
                        if (granted) {
                            fileSelectCallbk = filePathCallback
                            toAlbum(
                                this@WebActivity, 1, MimeType.of(
                                    MimeType.JPEG, MimeType.PNG
                                )
                            )
                        }
                    }
                return true
            }
        }

        web_view.loadUrl("file:///android_asset/test.html")
    }


    fun toAlbum(
        context: Activity,
        maxSelect: Int = 1,
        types: Set<MimeType> = MimeType.of(
            MimeType.JPEG, MimeType.PNG,
            MimeType.GIF, MimeType.MP4, MimeType.AVI, MimeType.MKV, MimeType.MPEG
        ),
        requestCode: Int = REQUEST_ALBUM
    ) {
        Matisse.from(context)
            .choose(types)
            .countable(true)
            .maxSelectable(maxSelect)
            .gridExpectedSize(
                240
            ) //图片显示表格的大小
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .theme(R.style.Matisse_Zhihu)
            .imageEngine(GlideEngine())
            .capture(true)
            .captureStrategy(CaptureStrategy(true, "com.landside.shadowstate.app.fileprovider"))
            .forResult(requestCode)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                val imgs = Matisse.obtainResult(data)
                lifecycleScope.launch {
                    imgs.map { it.toRealPath(this@WebActivity) }
                        .forEach {
                            fileSelectCallbk?.onReceiveValue(
                                arrayOf(
                                    Compressor.compress(this@WebActivity, File(it))
                                        .toUri()
                                )
                            )
                        }
                }
            } else {
                fileSelectCallbk?.onReceiveValue(null)
            }
        }
    }

}