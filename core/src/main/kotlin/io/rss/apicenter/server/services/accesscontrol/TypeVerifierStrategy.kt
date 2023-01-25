package io.rss.apicenter.server.services.accesscontrol

import org.springframework.security.core.context.SecurityContextHolder
import kotlin.reflect.KClass

/**
 * Specifies how to control the access for a specific type (Dto or Entity)
 */
internal interface TypeVerifierStrategy<T : Any> {

    /** Only for binding the parametrized T with the given Implementation */
    fun getType(): KClass<T>

    /**
     * Pre-authorization - checks input parameters
     * @return whether user can proceed or not
     * */
    fun hasUserAccess(data: List<T>): Boolean

    /**
     * For filtering results of methods.
     * Instead of allow/deny behavior, it aims to filter out only the records that the user can't access
     */
    fun filterResultList(data: List<T>): List<T>

    fun getAuthoritiesString(): List<String> {
        return SecurityContextHolder.getContext().authentication.authorities.map { it.authority }
    }

}