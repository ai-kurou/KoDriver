package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface ReadoutHotkeyPreferencesRepository {
    public fun observeVirtualKeyCode(): Flow<Int>

    public suspend fun saveVirtualKeyCode(virtualKeyCode: Int)
}
