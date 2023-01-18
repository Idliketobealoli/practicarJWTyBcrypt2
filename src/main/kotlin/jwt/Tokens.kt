package jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import common.Persona
import java.util.*

private val algorithm: Algorithm = Algorithm.HMAC256("uwu")

fun create(persona: Persona): String {
    return JWT.create()
        .withClaim("nombre", persona.name)
        .withExpiresAt(Date(System.currentTimeMillis() + 120000))
        .sign(algorithm)
}

fun decode(token: String): DecodedJWT? {
    val verifier = JWT.require(algorithm).build()

    return try {
        verifier.verify(token)
    } catch (_: Exception) {
        null
    }
}