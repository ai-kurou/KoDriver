package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

public class ObserveServerIpUseCase(
    private val repository: ServerIpPreferencesRepository,
) {
    public operator fun invoke(): Flow<String?> = repository.serverIp()
}
