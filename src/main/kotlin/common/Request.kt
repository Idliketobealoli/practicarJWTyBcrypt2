package common

import kotlinx.serialization.Serializable

@Serializable
data class Request<T>(
    val token: String?,
    val content: T?,
    val type: Type
) {
    enum class Type {
        TOKEN,HOLA,EXIT
    }
}