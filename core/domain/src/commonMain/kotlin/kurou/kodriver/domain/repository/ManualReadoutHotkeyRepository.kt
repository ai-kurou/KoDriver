package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutRequest

public interface ManualReadoutHotkeyRepository {
    public fun pressStream(): Flow<ManualReadoutRequest>

    public fun registrationStateStream(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState>

    public suspend fun retryRegistration(
        simulatorId: String,
        setId: String,
    )
}
