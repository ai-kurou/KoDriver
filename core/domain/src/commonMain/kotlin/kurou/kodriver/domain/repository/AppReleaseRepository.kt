package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.AppRelease

interface AppReleaseRepository {
    suspend fun getLatestRelease(): AppRelease?
}
