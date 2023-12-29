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
        println("QuilttConnectorWebView init")
        println(context)
        visibility = View.VISIBLE
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        this.settings.javaScriptEnabled = true
        this.settings.domStorageEnabled = true
//        TODO: Test on real device, might not need this/
//        this.settings.builtInZoomControls = false
    }

    fun load(
        token: String?,
        config: QuilttConnectorConfiguration,
        onEvent: ConnectorSDKOnEventCallback? = null,
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
            onExitSuccess = onExitSuccess,
            onExitAbort = onExitAbort,
            onExitError = onExitError
        )
        this.webViewClient = QuilttConnectorWebViewClient(clientParams)
        val url = "https://${config.connectorId}.quiltt.app/?mode=webview&oauth_redirect_url=${config.oauthRedirectUrl}&sdk=kotlin"
        this.loadUrl(url)
    }
}

data class QuilttConnectorWebViewClientParams(
    val context: Context,
    val webView: WebView,
    val config: QuilttConnectorConfiguration,
    val token: String?,
    val onEvent: ConnectorSDKOnEventCallback? = null,
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
               params.onExitAbort?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
           }
           "ExitError" -> {
               clearLocalStorage()
               params.onExitError?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = null))
           }
           "ExitSuccess" -> {
               clearLocalStorage()
               if (connectionId != null) {
                   params.onExitSuccess?.invoke(ConnectorSDKCallbackMetadata(connectorId = connectorId, profileId = null, connectionId = connectionId))
               }
           }
           "Authenticate" -> {
               println("Authenticate $profileId")
           }
           "OauthRequested" -> {
               val oauthUrlString = urlComponents.getQueryParameter("oauthUrl")
               if (oauthUrlString != null && URLUtil.isHttpsUrl(oauthUrlString)) {
                   handleOAuthUrl(Uri.parse(oauthUrlString))
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

        val script = """
            const options = {
            source: 'quiltt',
            type: 'Options',
            token: '$tokenString',
            connectorId: '$connectorId',
            connectionId: '$connectionId',
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
       "cdn.plaid.com/link/v2/stable/link.html",
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
       if (!URLUtil.isHttpsUrl(oauthUrl.toString())) {
           println("handleOAuthUrl - Skipping non https url - $oauthUrl")
           return
       }
       // Open the URL in the system browser
       val intent = Intent(Intent.ACTION_VIEW, oauthUrl)
       params.context.startActivity(intent)
   }

    private fun isQuilttEvent(url: Uri): Boolean {
        return url.toString().startsWith("quilttconnector://")
    }
}