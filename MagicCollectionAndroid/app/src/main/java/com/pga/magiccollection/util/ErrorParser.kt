package com.pga.magiccollection.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException
import java.io.IOException

/**
 * Códigos de error del backend para mostrar mensajes específicos
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
     * Parsea errores del backend y devuelve un mensaje legible para el usuario.
     * Prioriza el campo "error" del JSON, pero también interpreta códigos de error
     * para casos especiales como sesión expirada.
     */
    fun parseError(e: Throwable): String {
        return when (e) {
            is HttpException -> parseHttpException(e)
            is IOException -> "Sin conexión: verifica tu internet."
            else -> e.message ?: "Ha ocurrido un error inesperado."
        }
    }

    /**
     * Parsea el código de error si existe, útil para tomar decisiones en el ViewModel
     * como cerrar sesión automáticamente si el token expiró.
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

            // Obtener mensaje de error del backend
            val errorMessage = errorMap["error"]?.toString()
                ?: errorMap["message"]?.toString()

            // Si hay mensaje, usarlo directamente
            if (!errorMessage.isNullOrBlank()) {
                return errorMessage
            }

            // Si hay código de error, generar mensaje apropiado
            val errorCode = errorMap["code"]?.toString()
            if (errorCode != null) {
                return getMessageForCode(errorCode, e.code())
            }

            // Fallback por código HTTP
            getMessageForHttpCode(e.code())
        } catch (ex: Exception) {
            getMessageForHttpCode(e.code())
        }
    }

    private fun getMessageForCode(code: String, httpStatus: Int): String {
        return when (code) {
            ErrorCode.TOKEN_EXPIRED -> "La sesión ha expirado. Por favor, inicia sesión nuevamente."
            ErrorCode.TOKEN_INVALID -> "Token de autenticación inválido."
            ErrorCode.TOKEN_MISSING -> "Se requiere autenticación para esta acción."
            ErrorCode.CREDENTIALS_INVALID -> "Usuario o contraseña incorrectos."
            ErrorCode.SESSION_NOT_FOUND -> "No hay una sesión activa."
            ErrorCode.CURRENT_PASSWORD_INCORRECT -> "La contraseña actual es incorrecta."
            ErrorCode.ACCESS_DENIED -> "No tienes permisos para realizar esta acción."
            ErrorCode.USER_NOT_FOUND -> "Usuario no encontrado."
            ErrorCode.USERNAME_ALREADY_EXISTS -> "El nombre de usuario ya está en uso."
            ErrorCode.VALIDATION_ERROR -> "Datos inválidos. Revisa la información ingresada."
            ErrorCode.INTERNAL_ERROR -> "Error del servidor. Inténtalo más tarde."
            ErrorCode.EXTERNAL_SERVICE_ERROR -> "Servicio temporalmente no disponible."
            else -> getMessageForHttpCode(httpStatus)
        }
    }

    private fun getMessageForHttpCode(code: Int): String {
        return when (code) {
            400 -> "Solicitud inválida. Revisa los datos ingresados."
            401 -> "Credenciales incorrectas o sesión expirada."
            403 -> "No tienes permisos para esta acción."
            404 -> "Recurso no encontrado."
            409 -> "Ya existe un recurso con esos datos."
            500 -> "Error del servidor. Inténtalo más tarde."
            502 -> "Servicio temporalmente no disponible."
            503 -> "Servidor en mantenimiento. Inténtalo más tarde."
            else -> "Error inesperado ($code)"
        }
    }

    /**
     * Verifica si el error indica que la sesión expiró o es inválida,
     * útil para decidir si cerrar la sesión automáticamente.
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

