package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState

public interface HotkeyRepository {
    public suspend fun retryRegistration()

    public fun registrationStateStream(): Flow<HotkeyRegistrationState>

    public fun hotkeyPressStream(): Flow<Unit>
}
