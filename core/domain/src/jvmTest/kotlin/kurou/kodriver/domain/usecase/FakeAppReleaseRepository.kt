package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AppRelease
import kurou.kodriver.domain.repository.AppReleaseRepository

class FakeAppReleaseRepository(private val release: AppRelease?) : AppReleaseRepository {
    override suspend fun getLatestRelease(): AppRelease? = release
}
