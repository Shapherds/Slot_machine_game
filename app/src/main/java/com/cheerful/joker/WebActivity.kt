package com.cheerful.joker

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cheerful.joker.databinding.ActivityWebBinding
import com.onesignal.OneSignal
import java.util.*

class WebActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    private var mCustomView: View? = null
    private lateinit var mWebChromeClient : WebViewChromeClient
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri>? = null
    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1
    private var mCurrentUrl: String? = null

    private lateinit var userInterface : ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInterface = ActivityWebBinding.inflate(layoutInflater)
        setContentView(userInterface.root)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        // OneSignal Initialization
        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(AppConstants.APP_ID.data)
        customViewContainer = userInterface.customViewContainer
        webView = userInterface.webView
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView!!.settings.run {
            domStorageEnabled = true
            javaScriptEnabled = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = false
            loadsImagesAutomatically = true
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = true
            useWideViewPort = true
            displayZoomControls = false
            allowFileAccess = true
            setAppCacheEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = false
        }
        CookieManager.getInstance().run {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true);
        }
        webView!!.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        val webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (mCurrentUrl != null && url == mCurrentUrl) {
                    webView!!.goBack()
                    return true
                }
                view.loadUrl(url)
                mCurrentUrl = url
                return true
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                if (mCurrentUrl != null && request.url.toString() == mCurrentUrl) {
                    webView!!.goBack()
                    return true
                }
                view.loadUrl(request.url.toString())
                mCurrentUrl = request.url.toString()
                return true
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                val builder = AlertDialog.Builder(this@WebActivity)
                var message = "SSL Certificate error."
                when (error.primaryError) {
                    SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> message = "The certificate has expired."
                    SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                    SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
                }
                message += "Do you want to continue anyway?"
                builder.run {
                    setTitle("SSL Certificate Error")
                    setMessage(message)
                    setPositiveButton("continue") { _: DialogInterface?, _: Int -> handler.proceed() }
                    setNegativeButton("cancel") { _: DialogInterface?, _: Int -> handler.cancel() }
                }
                val dialog = builder.create()
                dialog.show()
            }
        }

        mWebChromeClient = WebViewChromeClient()

        webView!!.webChromeClient = mWebChromeClient
        webView!!.webViewClient = webViewClient

        webView!!.loadUrl(intent.getStringExtra("Links").toString())
    }

    open inner class WebViewChromeClient : WebChromeClient() {
        private var mVideoProgressView: View? = null

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(null)
                uploadMessage = null
            }
            uploadMessage = filePathCallback
            val intent =
                Intent("android.intent.action.GET_CONTENT").addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("image/*")
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE)
            } catch (e: ActivityNotFoundException) {
                uploadMessage = null
                Toast.makeText(this@WebActivity, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            mCustomView = view
            webView?.visibility = View.GONE
            customViewContainer?.visibility = View.VISIBLE
            customViewContainer?.addView(view)
            customViewCallback = callback
        }

        @SuppressLint("InflateParams")
        override fun getVideoLoadingProgressView(): View? {
            if (mVideoProgressView == null) {
                val inflater = LayoutInflater.from(this@WebActivity)
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null)
            }
            return mVideoProgressView
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            if (mCustomView == null) return
            webView?.visibility = View.VISIBLE
            customViewContainer?.visibility = View.GONE
            mCustomView!!.visibility = View.GONE
            customViewContainer?.removeView(mCustomView)
            customViewCallback?.onCustomViewHidden()
            mCustomView = null
        }
    }

    private fun inCustomView(): Boolean {
        return mCustomView != null
    }

    private fun hideCustomView() {
        mWebChromeClient.onHideCustomView()
    }
    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return
            val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } else Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_LONG).show()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView!!.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView!!.restoreState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (inCustomView()) {
            hideCustomView()
            return
        }
        if (mCustomView == null && webView!!.canGoBack()) {
            webView!!.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        webView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView!!.onResume()
    }

    override fun onStop() {
        super.onStop()
        if (inCustomView()) {
            hideCustomView()
        }
    }
}