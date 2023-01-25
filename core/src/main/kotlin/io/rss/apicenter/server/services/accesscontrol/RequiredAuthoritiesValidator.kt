package io.rss.apicenter.server.services.accesscontrol

import io.rss.experimental.cleanUpStack
import io.rss.apicenter.server.persistence.entities.RequiredAuthorities
import io.rss.apicenter.server.services.to.QueryResult
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
        typeVerifierStrategies: List<TypeVerifierStrategy<*>>
) {

    private val verifierStrategies: Map<KClass<out Any>, TypeVerifierStrategy<Any>>

    init {
        verifierStrategies =
                typeVerifierStrategies.associateBy({ it.getType() }, { it }) as Map<KClass<out Any>, TypeVerifierStrategy<Any>>
    }

    @Around("@annotation(io.rss.apicenter.server.services.accesscontrol.AssertRequiredAuthorities)")
    fun invoke(joi: ProceedingJoinPoint) = try {
        processInvocation(joi)
    } catch (e: Exception) {
        logger.warn("Error around AuthoritiesValidator proceeding function", e.cleanUpStack())
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

        val callResult = joi.proceed()

        if (callResult != null) {
            val effectiveResult = if (callResult is QueryResult<*>) {
                callResult.result
            } else {
                callResult
            }

            if (effectiveResult is List<*> && (effectiveResult as List<Any>).isNotEmpty()) {
                val filteredResult = filterCollectionResult(effectiveResult)
                return if (callResult is QueryResult<*>) {
                    QueryResult(filteredResult, false)
                } else {
                    filteredResult
                }
            }

            if (!hasUserAccess(callResult::class, listOf(effectiveResult))) {
                logger.warn("Access denied for the result in ${joi.signature.name}")
                throw AccessDeniedException("One or more of given resources are restricted: ${joi.signature.name}")
            }
        }

        return callResult
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