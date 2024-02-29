package app.quiltt.connector

import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class UsernamePayload {
    data class Email(val email: String) : UsernamePayload()
    data class Phone(val phone: String) : UsernamePayload()
}

data class PasscodePayload(val usernamePayload: UsernamePayload, val passcode: String)

data class SessionData(val token: String)
data class UnauthorizedData(val message: String, val instruction: String)
data class UnprocessableData(val attribute: Map<String, String>)

sealed class PingResponse {
    data class SessionResponse(val status: Int, val data: SessionData) : PingResponse()
    data class UnprocessableResponse(val status: Int, val data: UnprocessableData) : PingResponse()
}

sealed class IdentifyResponse {
    data class SessionResponse(val status: Int, val data: SessionData) : IdentifyResponse()
    data class AcceptedResponse(val status: Int) : IdentifyResponse()
    data class UnprocessableResponse(val status: Int, val data: UnprocessableData) : IdentifyResponse()
}

sealed class AuthenticateResponse {
    data class SessionResponse(val status: Int, val data: SessionData) : AuthenticateResponse()
    data class UnauthorizedResponse(val status: Int, val data: UnauthorizedData) : AuthenticateResponse()
    data class UnprocessableResponse(val status: Int, val data: UnprocessableData) : AuthenticateResponse()
}

sealed class RevokeResponse {
    data class NoContentResponse(val status: Int) : RevokeResponse()
    data class UnauthorizedResponse(val status: Int, val data: UnauthorizedData) : RevokeResponse()
}

class QuilttAuthApi(private val clientId: String?) {
    private val endpointAuth = "https://auth.quiltt.app/v1/users/session"

    /**
     * Response Statuses:
     *  - 200: OK           -> Session is Valid
     *  - 401: Unauthorized -> Session is Invalid
     */
    fun ping(token: String): PingResponse {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Authorization", "Bearer $token")

        val statusCode = connection.responseCode

        if (statusCode == 200) {
            return PingResponse.SessionResponse(statusCode, SessionData(token))
        }
        val inputStream = connection.errorStream
        val errorData = errorMap(inputStream)
        return PingResponse.UnprocessableResponse(statusCode, errorData)
    }

    /**
     * Response Statuses:
     *  - 201: Created              -> Profile Created, New Session Returned
     *  - 202: Accepted             -> Profile Found, MFA Code Sent for `authenticate`
     *  - 422: Unprocessable Entity -> Invalid Payload
     */
    fun identify(payload: UsernamePayload): IdentifyResponse {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.doOutput = true

        val jsonPayload = JSONObject()
        jsonPayload.put("clientId", clientId)
        when (payload) {
            is UsernamePayload.Email -> jsonPayload.put("email", payload.email)
            is UsernamePayload.Phone -> jsonPayload.put("phone", payload.phone)
        }

        if (connection.responseCode == 201) {
            val inputStream = connection.inputStream
            val jsonObject = JSONObject(inputStream.bufferedReader().use { it.readText() })
            return IdentifyResponse.SessionResponse(connection.responseCode, SessionData(jsonObject.getString("token")))
        }
        if (connection.responseCode == 202) {
            return IdentifyResponse.AcceptedResponse(connection.responseCode)
        }
        val inputStream = connection.errorStream
        val errorData = errorMap(inputStream)
        return IdentifyResponse.UnprocessableResponse(connection.responseCode, errorData)
    }

    /**
     * Response Statuses:
     *  - 201: Created              -> MFA Validated, New Session Returned
     *  - 401: Unauthorized         -> MFA Invalid
     *  - 422: Unprocessable Entity -> Invalid Payload
     */
    fun authenticate(payload: PasscodePayload): AuthenticateResponse {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.doOutput = true

        val jsonPayload = JSONObject()
        jsonPayload.put("clientId", clientId)
        jsonPayload.put("payload", payload)

        if (connection.responseCode == 201) {
            val inputStream = connection.inputStream
            val jsonObject = JSONObject(inputStream.bufferedReader().use { it.readText() })
            return AuthenticateResponse.SessionResponse(connection.responseCode, SessionData(jsonObject.getString("token")))
        }
        if (connection.responseCode == 401) {
            val inputStream = connection.errorStream
            val errorData = errorMap(inputStream) as UnauthorizedData
            return AuthenticateResponse.UnauthorizedResponse(connection.responseCode, errorData)
        }
        val inputStream = connection.errorStream
        val errorData = errorMap(inputStream)
        return AuthenticateResponse.UnprocessableResponse(connection.responseCode, errorData)
    }

    /**
     * Response Statuses:
     *  - 204: No Content   -> Session Revoked
     *  - 401: Unauthorized -> Session Not Found
     */
    fun revoke(token: String): RevokeResponse {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.responseCode

        if (connection.responseCode == 204) {
            return RevokeResponse.NoContentResponse(connection.responseCode)
        }
        val inputStream = connection.errorStream
        val errorData = errorMap(inputStream) as UnauthorizedData
        return RevokeResponse.UnauthorizedResponse(connection.responseCode, errorData)
    }

    private fun errorMap(inputStream: InputStream): UnprocessableData {
        val jsonObject = JSONObject(inputStream.bufferedReader().use { it.readText() })
        val errorMap = mutableMapOf<String, String>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getString(key)
            errorMap[key] = value
        }
        return UnprocessableData(errorMap)
    }
}
