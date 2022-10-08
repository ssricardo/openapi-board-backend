package io.rss.openapiboard.server.helper

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.rss.openapiboard.server.services.to.SubscriptionMailId
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/** Performs converting between Object <> String for User data */
object TokenHelper {

    private const val DUMMY_PASSWORD = "dummy"      // Filler for password field on User. Not needed after JWT auth
    private const val OAB_ISSUER: String = "oaBoard"
    private const val DAYS_VALID_LOGIN = 5L
    private const val DAYS_VALID_MAIL = 2L
    private const val ROLES_ATTRIBUTE = "roles"

    private lateinit var algorithmHS: Algorithm

    fun setupAlgorithm(secretKey: String) {
        this.algorithmHS = Algorithm.HMAC256(secretKey)
    }

    /** Exports the Authentication to a format to be transfered to the client */
    fun convertToString(input: Authentication): String {
        val expire = LocalDateTime.now().plusDays(DAYS_VALID_LOGIN)
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

    fun generateMailToken(info: SubscriptionMailId): String {
        val expire = LocalDateTime.now().plusDays(DAYS_VALID_MAIL)
        return JWT.create()
                .withIssuer(OAB_ISSUER)
                .withExpiresAt(Date.from(expire.atZone(ZoneId.systemDefault()).toInstant()))
                .withClaim(info::appName.name, info.appName)
                .withClaim(info::email.name, info.email)
                .sign(algorithmHS)
    }

    /** Retrieves Subscription information if token is valid.
     * @throws IllegalArgumentException If token is invalid
     * */
    fun validateRetrieveMailInfo(token: String): SubscriptionMailId {
        val verifier = JWT.require(algorithmHS).withIssuer(OAB_ISSUER).build()
        val jwtValue: DecodedJWT = verifier.verify(token);

        val apiName = (jwtValue.getClaim(SubscriptionMailId::appName.name)?.asString()
                ?: throw IllegalArgumentException("Invalid token"))
        val email = (jwtValue.getClaim(SubscriptionMailId::email.name)?.asString()
                ?: throw IllegalArgumentException("Invalid token"))

        return SubscriptionMailId(apiName, email)
    }

}
