package app.quiltt.connector

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class QuilttConnectorWebView(context: Context) : WebView(context) {
    init {
        visibility = View.VISIBLE
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        this.settings.javaScriptEnabled = true
        this.settings.domStorageEnabled = true
    }

    fun load(
        token: String?,
        config: QuilttConnectorConfiguration,
        onEvent: ConnectorSDKOnEventCallback? = null,
        onExit: ConnectorSDKOnEventExitCallback? = null,
        onExitSuccess: ConnectorSDKOnExitSuccessCallback? = null,
        onExitAbort: ConnectorSDKOnExitAbortCallback? = null,
        onExitError: ConnectorSDKOnExitErrorCallback? = null
    ) {
        val clientParams = QuilttConnectorWebViewClientParams(
            context = context,
            webView = this,
            config = config,
            token = token,
            onEvent = onEvent,
            onExit = onExit,
            onExitSuccess = onExitSuccess,
            onExitAbort = onExitAbort,
            onExitError = onExitError
        )
        this.webViewClient = QuilttConnectorWebViewClient(clientParams)
        
        // Apply smart URL encoding to the redirect URL
        val safeOAuthRedirectUrl = UrlUtils.smartEncodeURIComponent(config.oauthRedirectUrl)
        
        // Build the URL using Uri.Builder to properly handle parameter encoding
        val urlBuilder = Uri.Builder()
            .scheme("https")
            .authority("${config.connectorId}.quiltt.app")
            .appendQueryParameter("mode", "webview")
            .appendQueryParameter("agent", "android-${quilttSdkVersion}")
        
        // Handle the OAuth redirect URL with special care
        if (UrlUtils.isEncoded(safeOAuthRedirectUrl)) {
            // If already encoded, decode once to prevent double encoding
            val decodedOnce = Uri.decode(safeOAuthRedirectUrl)
            urlBuilder.appendQueryParameter("oauth_redirect_url", decodedOnce)
        } else {
            urlBuilder.appendQueryParameter("oauth_redirect_url", safeOAuthRedirectUrl)
        }
        
        val url = urlBuilder.build().toString()
        this.loadUrl(url)
    }
}

data class QuilttConnectorWebViewClientParams(
    val context: Context,
    val webView: WebView,
    val config: QuilttConnectorConfiguration,
    val token: String?,
    val onEvent: ConnectorSDKOnEventCallback? = null,
    val onExit: ConnectorSDKOnEventExitCallback? = null,
    val onExitSuccess: ConnectorSDKOnExitSuccessCallback? = null,
    val onExitAbort: ConnectorSDKOnExitAbortCallback? = null,
    val onExitError: ConnectorSDKOnExitErrorCallback? = null
)

class QuilttConnectorWebViewClient(private val params: QuilttConnectorWebViewClientParams) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url
        println(url.toString())
        if (isQuilttEvent(url)) {
            handleQuilttEvent(url)
            return true
        }
        if (shouldRender(url)) {
            return false
        }
        handleOAuthUrl(url)
        return true
    }

    private fun handleQuilttEvent(url: Uri) {
        val urlComponents = Uri.parse(url.toString())
        val connectorId = params.config.connectorId
        val profileId = urlComponents.getQueryParameter("profileId")
        val connectionId = urlComponents.getQueryParameter("connectionId")
        println("handleQuilttEvent $url")
        when (url.host) {
            "Load" -> {
                initInjectJavaScript()
            }
            "ExitAbort" -> {
                clearLocalStorage()
                params.onExit?.invoke(ConnectorSDKEventType.ExitAbort, ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
                params.onExitAbort?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
            }
            "ExitError" -> {
                clearLocalStorage()
                params.onExit?.invoke(ConnectorSDKEventType.ExitError, ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
                params.onExitError?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
            }
            "ExitSuccess" -> {
                clearLocalStorage()
                if (connectionId != null) {
                    params.onExit?.invoke(ConnectorSDKEventType.ExitSuccess, ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = connectionId))
                    params.onExitSuccess?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = connectionId))
                }
            }
            "Authenticate" -> {
                println("Authenticate $profileId")
            }
            "Navigate" -> {
                val navigateUrlString = urlComponents.getQueryParameter("url")
                if (navigateUrlString != null) {
                    // Handle potential encoding issues
                    if (UrlUtils.isEncoded(navigateUrlString)) {
                        try {
                            // If encoded, decode once to prevent double-encoding
                            val decodedUrl = Uri.decode(navigateUrlString)
                            if (URLUtil.isHttpsUrl(decodedUrl)) {
                                handleOAuthUrl(Uri.parse(decodedUrl))
                            }
                        } catch (error: Exception) {
                            println("Navigate URL decoding failed, using original")
                            if (URLUtil.isHttpsUrl(navigateUrlString)) {
                                handleOAuthUrl(Uri.parse(navigateUrlString))
                            }
                        }
                    } else if (URLUtil.isHttpsUrl(navigateUrlString)) {
                        handleOAuthUrl(Uri.parse(navigateUrlString))
                    }
                } else {
                    println("Navigate URL missing from request")
                }
            }
            else -> {
                println("unhandled event $url")
            }
        }
    }

    private fun initInjectJavaScript() {
        val tokenString = params.token ?: "null"
        val connectorId = params.config.connectorId
        val connectionId = params.config.connectionId ?: "null"
        val institution = params.config.institution ?: "null"

        val script = """
            const options = {
            source: 'quiltt',
            type: 'Options',
            token: '$tokenString',
            connectorId: '$connectorId',
            connectionId: '$connectionId',
            institution: '$institution',
            };
            const compactedOptions = Object.keys(options).reduce((acc, key) => {
            if (options[key] !== 'null') {
                acc[key] = options[key];
            }
            return acc;
            }, {});
            window.postMessage(compactedOptions);
        """

        params.webView.evaluateJavascript(script, null)
    }

    private fun clearLocalStorage() {
        val script = "localStorage.clear();"
        params.webView.evaluateJavascript(script, null)
    }

    private val allowedListUrl = listOf(
        "quiltt.app",
        "quiltt.dev",
        "moneydesktop.com",
        "cdn.plaid.com",
        "finicity.com",
    )

    private fun shouldRender(url: Uri): Boolean {
        if (isQuilttEvent(url)) {
            return false
        }
        for (allowedUrl in allowedListUrl) {
            if (url.toString().contains(allowedUrl)) {
                return true
            }
        }
        return false
    }

    private fun handleOAuthUrl(oauthUrl: Uri) {
        val urlString = oauthUrl.toString()
        
        if (!URLUtil.isHttpsUrl(urlString)) {
            println("handleOAuthUrl - Skipping non https url - $oauthUrl")
            return
        }
        
        // Normalize the URL encoding to prevent double-encoding issues
        val normalizedUrl = UrlUtils.normalizeUrlEncoding(urlString)
        
        // Open the URL in the system browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl))
        params.context.startActivity(intent)
    }

    private fun isQuilttEvent(url: Uri): Boolean {
        return url.toString().startsWith("quilttconnector://")
    }
}