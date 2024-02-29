package app.quiltt.app_quiltthub_webview_example

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidView
import app.quiltt.connector.QuilttAuthApi
import android.app.AlertDialog
import android.net.Uri
import android.webkit.WebResourceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource

class QuilttHubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuilttHubContent()
        }
    }

    override fun onBackPressed() {
        showLogoutConfirmationDialog()
    }
    fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
//                startActivity((Intent(this, MainActivity::class.java)))
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun launchWithCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }
    private fun logout() {
        val token = SharedPreferencesHelper(context = this).getData("token")
        val apiClient = QuilttAuthApi(clientId = null)
        CoroutineScope(Dispatchers.IO).launch {
            apiClient.revoke(token = token!!)
        }
        this.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuilttHubContent() {
    val context = LocalContext.current
    val activity = context as QuilttHubActivity
    val token = SharedPreferencesHelper(context = context).getData("token")
    val url = "https://www.quiltthub.com/login?mode=webview&token=$token"
    Column {
        TopAppBar(
            title = {
                Image(
                    painter = painterResource(id = R.drawable.quiltt_app_icon),
                    contentDescription = "Quiltt Hub Logo",
                    modifier = Modifier.size(40.dp))
            },
            actions = {
                Button(onClick = {
                    val intent = Intent(context, QuilttConnectorActivity::class.java)
                    context.startActivity(intent)
                },
                    shape = RoundedCornerShape(25),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.purple_500),
                        contentColor = Color.White))
                {
                    Text("Add Account")
                }
                IconButton(onClick = {
                    activity.showLogoutConfirmationDialog()
                }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                }
            }
        )
        WebViewComposable(url = url)
    }
}

@Composable
fun WebViewComposable(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                val activity = context as QuilttHubActivity
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString()
                    if (url != null && url.startsWith("https://www.quiltthub.com/mobile/hub/reconnect")) {
                        val urlComponents = Uri.parse(url)
                        val connectionId = urlComponents.getQueryParameter("connectionId")
                        val intent = Intent(context, QuilttConnectorActivity::class.java)
                        intent.putExtra("connectionId", connectionId)
                        activity.startActivity(intent)
                        return true
                    }
                    if (url != null && url.startsWith("https://www.quiltthub.com")) {
                        return false
                    }
                    // TODO: Sometimes connector is publishing Options?
                    if (url != null && url.startsWith("quilttconnector://")) {
                        return true
                    }
                    // TODO: handle token expires while the user is using the app

                    // Handle quiltt.io links, mailto links and other social media links
                    if (url != null) {
                        activity.launchWithCustomTab(url)
                        return true
                    }
                    // Return false to let the WebView handle the URL
                    return false
                }
            }
            loadUrl(url)
        }
    }, update = { webView ->
        webView.loadUrl(url)
    })
}

@Preview(showBackground = true)
@Composable
fun QuilttHubActivityPreview() {
    QuilttHubContent()
}