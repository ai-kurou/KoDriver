package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.AppUpdate

interface AppUpdateRepository {
    suspend fun getLatestRelease(): AppUpdate?
}
