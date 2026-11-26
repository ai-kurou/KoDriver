package kurou.kodriver.domain.serverconnection.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository

public class ObserveServerIpUseCase(
    private val repository: ServerIpPreferencesRepository,
) {
    public operator fun invoke(): Flow<String?> = repository.serverIp()
}
