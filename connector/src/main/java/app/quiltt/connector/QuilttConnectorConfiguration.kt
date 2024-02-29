package app.quiltt.connector

interface QuilttConnectorConfiguration {
    val connectorId: String
    val oauthRedirectUrl: String?
    val connectionId: String?
}

data class QuilttConnectorConnectConfiguration(
    override val connectorId: String,
    override val oauthRedirectUrl: String? = null
) : QuilttConnectorConfiguration {
    override val connectionId: String? = null // always null for connect, cannot be set
}

data class QuilttConnectorReconnectConfiguration(
    override val connectorId: String,
    override val oauthRedirectUrl: String,
    override var connectionId: String?
) : QuilttConnectorConfiguration