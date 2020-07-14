package io.rss.openapiboard.server.helper

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/** Performs converting between Object <> String for User data */
object AuthHelper {

    private const val DUMMY_PASSWORD = "dummy"
    private const val OAB_ISSUER: String = "oaBoard"
    private const val DAYS_VALID_TOKEN = 5L
    private const val ROLES_ATTRIBUTE = "roles"

    private const val TEMP_HARDCODED_NOT_SO_SECRET = "DXnC^E8xFm6-ectU4rXv8T7jNnxW2rdXazwxQvWwshCiEzcLQvMbDYAKECFBtyWWzqQnv5tzpGoWRdbNxXPmaLKR/jSx_FSWoij88uHaLu6zVQPfvPRUwAmRimqcUTba"

    private val algorithmHS: Algorithm = Algorithm.HMAC256(TEMP_HARDCODED_NOT_SO_SECRET)

    /** Exports the Authentication to a format to be transfered to the client */
    fun convertToString(input: Authentication): String {
        val expire = LocalDateTime.now().plusDays(DAYS_VALID_TOKEN)
        return JWT.create()
                .withSubject(input.name)
                .withArrayClaim(ROLES_ATTRIBUTE, input.authorities.map { it.authority }.toTypedArray())
                .withIssuer(OAB_ISSUER)
                .withExpiresAt(Date.from(expire.atZone(ZoneId.systemDefault()).toInstant()))
                .sign(algorithmHS)
    }

    /** From a token, tries to get an User object */
    fun validateConvertToUser(token: String): User? {
        val verifier = JWT.require(algorithmHS).withIssuer(OAB_ISSUER).build();
        val jwtValue: DecodedJWT?
        try {
            jwtValue = verifier.verify(token);
        } catch (e: JWTVerificationException) {
            throw BadCredentialsException("Invalid token", e)
        }
        val roles = jwtValue.getClaim(ROLES_ATTRIBUTE).asList(String::class.java)
                .mapTo(mutableListOf()) { r -> SimpleGrantedAuthority(r) }
        return User(jwtValue.subject, DUMMY_PASSWORD, roles)
    }
}
