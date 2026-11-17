package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.AppUpdate

public interface AppUpdateRepository {
    public suspend fun getLatestRelease(): AppUpdate?
}
