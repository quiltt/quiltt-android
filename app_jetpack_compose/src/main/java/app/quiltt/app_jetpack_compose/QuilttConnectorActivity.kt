package app.quiltt.app_jetpack_compose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import app.quiltt.connector.QuilttConnector
import app.quiltt.connector.QuilttConnectorConnectConfiguration
import app.quiltt.connector.QuilttConnectorReconnectConfiguration

class QuilttConnectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectionId = intent.getStringExtra("connectionId")
        val connectorId = AppConfig.addConnectorId
        val oauthRedirectUrl = AppConfig.oauthRedirectUrl
        val token = SharedPreferencesHelper(context = this).getData("token")
        println("Connection ID: $connectionId")
        if (connectionId != null) {
            val config = QuilttConnectorReconnectConfiguration(
                connectorId = connectorId,
                oauthRedirectUrl = oauthRedirectUrl,
                connectionId = connectionId
            )
            setContent {
                QuilttReconnectContent(config = config, token = token!!)
            }
        } else {
            val config = QuilttConnectorConnectConfiguration(
                connectorId = connectorId,
                oauthRedirectUrl = oauthRedirectUrl)
            setContent {
                QuilttConnectContent(config = config, token = token!!)
            }
        }

    }
}

@Composable
fun QuilttConnectContent(config: QuilttConnectorConnectConfiguration, token: String) {
    val context = LocalContext.current
    val quilttConnector = QuilttConnector(context)
    quilttConnector.authenticate(token)
    val connectorWebView = quilttConnector.connect(
        config = config,
        onExitSuccess = { metadata ->
            println("Exit success!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        },
        onExitAbort = { metadata ->
            println("Exit abort!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        },
        onExitError = { metadata ->
            println("Exit error!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        })
    AndroidView(factory = { connectorWebView } )
}

@Composable
fun QuilttReconnectContent(config: QuilttConnectorReconnectConfiguration, token: String) {
    val context = LocalContext.current
    val quilttConnector = QuilttConnector(context)
    quilttConnector.authenticate(token)
    val connectorWebView = quilttConnector.reconnect(
        config = config,
        onExitSuccess = { metadata ->
            println("Exit success!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        },
        onExitAbort = { metadata ->
            println("Exit abort!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        },
        onExitError = { metadata ->
            println("Exit error!")
            println("Metadata: $metadata")
            if (context is Activity) {
                context.finish()
            }
        })
    AndroidView(factory = { connectorWebView } )
}

@Preview(showBackground = true)
@Composable
fun QuilttConnectorPreview() {
    QuilttConnectContent(
        config = QuilttConnectorConnectConfiguration(
            connectorId = "<CONNECTOR_ID>",
            oauthRedirectUrl = "<YOUR_HTTP_APP_LINK>"
        ),
        token = "<TOKEN>"
    )
}