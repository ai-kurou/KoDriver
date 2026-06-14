package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ServerIpRepository

internal class FakeServerIpRepository(initialIp: String? = null) : ServerIpRepository {
    private val ip = MutableStateFlow(initialIp)

    override fun serverIp(): Flow<String?> = ip

    override suspend fun saveServerIp(ip: String) {
        this.ip.update { ip }
    }
}
