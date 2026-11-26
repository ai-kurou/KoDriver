package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.manualreadout.model.HotkeyRegistrationState
import kurou.kodriver.domain.manualreadout.model.ManualReadoutRequest
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutHotkeyRepository

internal class UnsupportedManualReadoutHotkeyRepository : ManualReadoutHotkeyRepository {
    override fun pressStream(): Flow<ManualReadoutRequest> = emptyFlow()

    override fun registrationStateStream(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration(
        simulatorId: String,
        setId: String,
    ) = Unit
}
