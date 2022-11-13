package io.rss.openapiboard.server.persistence.dao

import io.rss.openapiboard.server.persistence.entities.Namespace
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * As there won't be too many namespaces, we hold all of them in memory.
 * Be aware that there's a delay for updating the cache (see #initCache)
 */
@Repository
class NamespaceCachedRepository (
    private val namespaceRepository: NamespaceRepository,
) {

    private val cache = ConcurrentHashMap<String, List<String>>()

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.MINUTES)
    protected fun initCache() = updateCache()

    private fun updateCache() {
        val result: Map<String, List<String>> = namespaceRepository.findAllWithAuthorities()
                .fold(HashMap()) { map, ns ->
                    val auths = ns.requiredAuthorities.map { it.authority }
                    map[ns.name] = auths
                    map
                }

        cache.clear()
        cache.putAll(result)
    }

    fun saveOrUpdate(namespace: Namespace): Namespace {
        return namespaceRepository.save(namespace).also {
            updateCache()
        }
    }

    fun remove(name: String) {
        namespaceRepository.deleteById(name)
        updateCache()
    }

    fun exists(name: String): Boolean {
        return cache.containsKey(name)
    }

    /**
     * @return
     * Will be null if the namespace is not found.
     * Empty list, if the ns doesn't require any specific role
     * */
    fun getAuthorities(name: String): List<String>? {
        return cache[name]
    }

    fun findAll(): Set<String> {
        return cache.keys
    }
}