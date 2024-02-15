package app.quiltt.connector

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


data class EmailInput(val email: String)
data class PhoneInput(val phone: String)

sealed class UsernamePayload {
    data class Email(val email: String) : UsernamePayload()
    data class Phone(val phone: String) : UsernamePayload()
}

data class PasscodePayload(val usernamePayload: UsernamePayload, val passcode: String)

data class SessionData(val token: String)
data class UnprocessableData(val attribute: Map<String, String>)

sealed class AuthResponse {
    data class SessionResponse(val status: Int, val data: SessionData) : AuthResponse()
    data class UnprocessableResponse(val status: Int, val data: UnprocessableData) : AuthResponse()
}

class QuilttAuthApi(private val clientId: String?) {
    private val endpointAuth = "https://auth.quiltt.app/v1/users/session"

    /**
     * Response Statuses:
     *  - 200: OK           -> Session is Valid
     *  - 401: Unauthorized -> Session is Invalid
     */
    fun ping(token: String): AuthResponse {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.setRequestProperty("Authorization", "Bearer $token")

        val statusCode = connection.responseCode

        if (statusCode == 200) {
            return AuthResponse.SessionResponse(statusCode, SessionData(token))
        }
        val inputStream = connection.errorStream
        val errorData = errorMap(inputStream)
        return AuthResponse.UnprocessableResponse(statusCode, errorData)
    }

    private fun errorMap(inputStream: InputStream): UnprocessableData {
        println("errorMap inputStream: $inputStream")
        val jsonObject = JSONObject(inputStream.bufferedReader().use { it.readText() })
        println("errorMap jsonObject: $jsonObject")
        val errorMap = mutableMapOf<String, String>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getString(key)
            errorMap[key] = value
        }
        return UnprocessableData(errorMap)
    }

    fun identify(payload: UsernamePayload): SessionData {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.doInput = true
        connection.doOutput = true

        val jsonPayload = JSONObject()
        jsonPayload.put("clientId", clientId)
        jsonPayload.put("payload", payload)

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(jsonPayload.toString())
        writer.flush()
        writer.close()

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readLine()
        reader.close()

        val jsonObject = JSONObject(response)
        return SessionData(jsonObject.getString("token"))
    }

    fun authenticate(payload: PasscodePayload): SessionData {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        connection.doOutput = true

        val jsonPayload = JSONObject()
        jsonPayload.put("clientId", clientId)
        jsonPayload.put("payload", payload)

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(jsonPayload.toString())
        writer.flush()
        writer.close()

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readLine()
        reader.close()

        val jsonObject = JSONObject(response)
        return SessionData(jsonObject.getString("token"))
    }

    fun revoke(token: String) {
        val url = URL(endpointAuth)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.responseCode
        println("Revoke response code: ${connection.responseCode}")
    }
}
