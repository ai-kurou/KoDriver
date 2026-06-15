package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.ServerIpRepository
import java.io.IOException

internal class FakeServerIpRepository(
    initialIp: String? = null,
    var shouldThrow: Boolean = false,
) : ServerIpRepository {
    private val ip = MutableStateFlow(initialIp)

    override fun serverIp(): Flow<String?> = ip

    override suspend fun saveServerIp(ip: String) {
        if (shouldThrow) throw IOException("保存失敗")
        this.ip.update { ip }
    }
}
