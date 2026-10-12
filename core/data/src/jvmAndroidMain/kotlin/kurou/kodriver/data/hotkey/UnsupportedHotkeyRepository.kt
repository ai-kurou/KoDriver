package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.HotkeyRepository

internal class UnsupportedHotkeyRepository : HotkeyRepository {
    override suspend fun retryRegistration() = Unit

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override fun hotkeyPressStream(): Flow<Unit> = emptyFlow()
}
