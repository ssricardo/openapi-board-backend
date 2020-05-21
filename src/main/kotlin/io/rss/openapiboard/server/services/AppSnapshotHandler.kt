package io.rss.openapiboard.server.services

import io.rss.openapiboard.server.config.security.Roles
import io.rss.openapiboard.server.helper.assertStringRequired
import io.rss.openapiboard.server.persistence.dao.AppSnapshotRepository
import io.rss.openapiboard.server.persistence.entities.AppRecord
import io.rss.openapiboard.server.persistence.entities.AppRecordId
import io.rss.openapiboard.server.persistence.entities.AppSnapshot
import io.rss.openapiboard.server.persistence.entities.AppSnapshotId
import io.rss.openapiboard.server.services.exceptions.BoardApplicationException
import io.rss.openapiboard.server.services.to.AppComparison
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import javax.annotation.security.RolesAllowed
import javax.inject.Inject

/** Provides CRUD operations for AppSnapshop, making use of Async in some cases */

@Service
@PreAuthorize("hasAnyAuthority('${Roles.AGENT}', '${Roles.MANAGER}')")
class AppSnapshotHandler {

    @Inject
    lateinit var repository: AppSnapshotRepository

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AppSnapshotHandler::class.java)
    }

    /**
     * Stores a new Snapshot, <b>Async</b>
     */
    @Async
    fun feed(app: AppRecord) {
        assert(app.name != null && app.namespace != null)
        assert(app.version != null) {"""Version is required when registering an App.
                It's not possible to save it's history for ${app.name}"""}

        val snap = AppSnapshot(app.name, app.namespace, app.version).apply {
            this.source = app.source
            this.address = app.address
        }
        repository.save(snap)

        LOGGER.info("${app.name} registered")
    }

    /** Delegates searching of version list for given App/namespace */
    fun listVersionsByAppNamespace(app: String, namespace: String): List<String> {
        assertStringRequired(app) {"App name is required for this query"}
        assertStringRequired(namespace) {"Namespace is required for this query"}
        return repository.findAppVersionList(AppRecordId(app, namespace))
    }

    /** Search given snapshops and creates a comparison with them.
     * @throws BoardApplicationException if some of the Apps is not found
     * */
    fun createComparison(sourceApp: AppSnapshotId, targetApp: AppSnapshotId): AppComparison {
        val sourceResult = repository.findById(sourceApp)
        val comparedResult = repository.findById(targetApp)

        if (! sourceResult.isPresent || ! comparedResult.isPresent) {
            throw BoardApplicationException("Comparision not possible. One or both of specified App was not found")
        }

        return AppComparison(sourceResult.get(), comparedResult.get())
    }
}