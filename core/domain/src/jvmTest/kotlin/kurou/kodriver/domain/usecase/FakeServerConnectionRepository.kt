package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerConnectionRepository

internal class FakeServerConnectionRepository(
    private val connected: Boolean = true,
) : ServerConnectionRepository {
    override suspend fun isConnected(ip: String): Boolean = connected
}
