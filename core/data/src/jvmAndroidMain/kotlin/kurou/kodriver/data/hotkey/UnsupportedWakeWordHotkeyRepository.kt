package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository

internal class UnsupportedWakeWordHotkeyRepository : WakeWordHotkeyRepository {
    override fun pressStream(): Flow<Unit> = emptyFlow()

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration() = Unit
}
