package common

import kotlinx.serialization.Serializable
import serializers.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Persona(
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaNacimiento: LocalDateTime = LocalDateTime.now()
)
