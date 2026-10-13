package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState

interface HotkeyRepository {
    suspend fun retryRegistration()

    fun registrationStateStream(): Flow<HotkeyRegistrationState>

    fun hotkeyPressStream(): Flow<Unit>
}
