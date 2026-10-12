package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReadoutHotkeyPreferencesRepository {
    fun observeVirtualKeyCode(): Flow<Int>

    suspend fun saveVirtualKeyCode(virtualKeyCode: Int)
}
