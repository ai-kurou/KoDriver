package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository

class FakeAppUpdateRepository(private val release: AppUpdate?) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = release
}
