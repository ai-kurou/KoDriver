package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState

interface HotkeyRepository {
    fun observeVirtualKeyCode(): Flow<Int>

    suspend fun saveVirtualKeyCode(virtualKeyCode: Int)

    fun registrationStateStream(): Flow<HotkeyRegistrationState>

    fun hotkeyPressStream(): Flow<Unit>
}
