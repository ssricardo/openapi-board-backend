package io.rss.openapiboard.server.services.accesscontrol

import io.rss.openapiboard.server.persistence.entities.RequiredAuthorities
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Asserts that the inputs and outputs can be accessed by the user, based on given RequiredAuthorities associated to the entities.
 * @see RequiredAuthorities
 */
@Aspect
@Component
internal class RequiredAuthoritiesValidator (
        typeVerifierStrategies: List<TypeVerifierStrategy<Any>>
) {

    private val verifierStrategies: Map<KClass<out Any>, TypeVerifierStrategy<Any>>

    init {
        verifierStrategies = typeVerifierStrategies.associateBy({ it.getType() }, { it })
    }

    @Around("@annotation(io.rss.openapiboard.server.services.accesscontrol.AssertRequiredAuthorities)")
    fun invoke(joi: ProceedingJoinPoint) = try {
        processInvocation(joi)
    } catch (e: Exception) {
        logger.error("Error around AuthoritiesValidator proceeding function", e)
        throw e
    }

    private fun processInvocation(joi: ProceedingJoinPoint): Any? {
        val argsGroups = groupPresentArgsByType(joi)

        argsGroups.forEach { (key, value) ->
            if (!hasUserAccess(key, value)) {
                logger.warn("Access denied for argument $key, in ${joi?.signature?.name}")
                throw AccessDeniedException("One or more of given resources are restricted: $value")
            }
        }

        val result = joi.proceed()

        if (result != null) {
            if (result is List<*> && (result as List<Any>).isNotEmpty()) {
                return filterCollectionResult(result)
            }

            if (!hasUserAccess(result::class, listOf(result))) {
                logger.warn("Access denied for the result in ${joi.signature.name}")
                throw AccessDeniedException("One or more of given resources are restricted: ${joi.signature.name}")
            }
        }

        return result
    }

    private fun filterCollectionResult(result: List<Any>): List<Any> {
        return result.firstOrNull()
                ?.let { verifierStrategies[it::class] }?.filterResultList(result)
                ?: result
    }

    private fun groupPresentArgsByType(joi: ProceedingJoinPoint) = joi.args
            .asSequence()
            .filterNotNull()
            .groupBy { it::class }



    private fun <T : Any> hasUserAccess(clazz: KClass<T>, data: List<Any>): Boolean {
        val predicate = verifierStrategies[clazz]
                ?: return true

        return predicate.hasUserAccess(data)
    }

    private companion object {
        val logger = LoggerFactory.getLogger(RequiredAuthoritiesValidator::class.java)
    }
}