package app.quiltt.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import app.quiltt.connector.QuilttConnector
import app.quiltt.connector.QuilttConnectorConnectConfiguration

class QuilttConnectorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiltt_connector)
        val connectorLayout = findViewById<ConstraintLayout>(R.id.connector_layout)
        val quilttConnector = QuilttConnector(this)
        val quilttConnectorConfiguration = QuilttConnectorConnectConfiguration(
            connectorId = "mobile-sdk-sandbox",
            oauthRedirectUrl = "https://tom-quiltt.github.io/expo-redirect/kotlin")
        val webView = quilttConnector.connect(
            config = quilttConnectorConfiguration,
            onEvent = { eventType, metadata ->
                println("Event: $eventType")
                println("Metadata: $metadata")
            },
            onExitSuccess = { metadata ->
                println("Exit success!")
                println("Metadata: $metadata")
                finish()
            },
            onExitAbort = { metadata ->
                println("Exit abort!")
                println("Metadata: $metadata")
                finish()
            },
            onExitError = { metadata ->
                println("Exit error!")
                println("Metadata: $metadata")
                finish()
            })

        connectorLayout.addView(webView)
    }
}