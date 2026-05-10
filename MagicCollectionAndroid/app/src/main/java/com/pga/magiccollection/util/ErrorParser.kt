package com.pga.magiccollection.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException
import java.io.IOException

/**
 * Backend error codes to show specific messages
 */
object ErrorCode {
    const val TOKEN_EXPIRED = "TOKEN_EXPIRED"
    const val TOKEN_INVALID = "TOKEN_INVALID"
    const val TOKEN_MISSING = "TOKEN_MISSING"
    const val CREDENTIALS_INVALID = "CREDENTIALS_INVALID"
    const val SESSION_NOT_FOUND = "SESSION_NOT_FOUND"
    const val CURRENT_PASSWORD_INCORRECT = "CURRENT_PASSWORD_INCORRECT"
    const val ACCESS_DENIED = "ACCESS_DENIED"
    const val USER_NOT_FOUND = "USER_NOT_FOUND"
    const val USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
    const val EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR"
}

object ErrorParser {
    private val gson = Gson()

    /**
     * Parses backend errors and returns a user-friendly message.
     * Prioritizes the "error" field from JSON, but also interprets error codes
     * for special cases like expired sessions.
     */
    fun parseError(e: Throwable): String {
        return when (e) {
            is HttpException -> parseHttpException(e)
            is IOException -> "No connection: please check your internet."
            else -> e.message ?: "An unexpected error has occurred."
        }
    }

    /**
     * Parses the error code if it exists, useful for making decisions in the ViewModel
     * such as automatically logging out if the token expired.
     */
    fun parseErrorCode(e: Throwable): String? {
        if (e !is HttpException) return null
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = gson.fromJson(errorBody, mapType)
            errorMap["code"]?.toString()
        } catch (ex: Exception) {
            null
        }
    }

    private fun parseHttpException(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = gson.fromJson(errorBody, mapType)

            // Get error message from backend
            val errorMessage = errorMap["error"]?.toString()
                ?: errorMap["message"]?.toString()

            // If message exists, use it directly
            if (!errorMessage.isNullOrBlank()) {
                return errorMessage
            }

            // If error code exists, generate appropriate message
            val errorCode = errorMap["code"]?.toString()
            if (errorCode != null) {
                return getMessageForCode(errorCode, e.code())
            }

            // Fallback by HTTP code
            getMessageForHttpCode(e.code())
        } catch (ex: Exception) {
            getMessageForHttpCode(e.code())
        }
    }

    private fun getMessageForCode(code: String, httpStatus: Int): String {
        return when (code) {
            ErrorCode.TOKEN_EXPIRED -> "The session has expired. Please log in again."
            ErrorCode.TOKEN_INVALID -> "Invalid authentication token."
            ErrorCode.TOKEN_MISSING -> "Authentication is required for this action."
            ErrorCode.CREDENTIALS_INVALID -> "Incorrect username or password."
            ErrorCode.SESSION_NOT_FOUND -> "No active session found."
            ErrorCode.CURRENT_PASSWORD_INCORRECT -> "The current password is incorrect."
            ErrorCode.ACCESS_DENIED -> "You do not have permission to perform this action."
            ErrorCode.USER_NOT_FOUND -> "User not found."
            ErrorCode.USERNAME_ALREADY_EXISTS -> "Username is already in use."
            ErrorCode.VALIDATION_ERROR -> "Invalid data. Please check the entered information."
            ErrorCode.INTERNAL_ERROR -> "Server error. Please try again later."
            ErrorCode.EXTERNAL_SERVICE_ERROR -> "Service temporarily unavailable."
            else -> getMessageForHttpCode(httpStatus)
        }
    }

    private fun getMessageForHttpCode(code: Int): String {
        return when (code) {
            400 -> "Invalid request. Please check the entered data."
            401 -> "Incorrect credentials or expired session."
            403 -> "You do not have permission for this action."
            404 -> "Resource not found."
            409 -> "A resource with this data already exists."
            500 -> "Server error. Please try again later."
            502 -> "Service temporarily unavailable."
            503 -> "Server under maintenance. Please try again later."
            else -> "Unexpected error ($code)"
        }
    }

    /**
     * Checks if the error indicates the session expired or is invalid,
     * useful for deciding whether to log out automatically.
     */
    fun isSessionError(e: Throwable): Boolean {
        val code = parseErrorCode(e)
        return code in listOf(
            ErrorCode.TOKEN_EXPIRED,
            ErrorCode.TOKEN_INVALID,
            ErrorCode.TOKEN_MISSING,
            ErrorCode.SESSION_NOT_FOUND
        )
    }
}
