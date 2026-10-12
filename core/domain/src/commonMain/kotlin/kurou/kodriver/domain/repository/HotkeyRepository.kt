package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface HotkeyRepository {
    fun hotkeyPressStream(): Flow<Unit>
}
