package kurou.kodriver.domain.repository

import kurou.kodriver.core.model.AppUpdate

interface AppUpdateRepository {
    suspend fun getLatestRelease(): AppUpdate?
}
