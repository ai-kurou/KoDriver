package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutRequest

interface ManualReadoutHotkeyRepository {
    fun pressStream(): Flow<ManualReadoutRequest>

    fun registrationStateStream(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState>

    suspend fun retryRegistration(
        simulatorId: String,
        setId: String,
    )
}
