package kurou.kodriver.feature.manualreadoutdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository

/** ウェイクワードホットキーが未割り当ての状態を返すテスト用の Repository。 */
internal class UnassignedWakeWordHotkeyPreferencesRepository : WakeWordHotkeyPreferencesRepository {
    override fun observeWakeWordHotkey(): Flow<WakeWordHotkey> = flowOf(WakeWordHotkey())

    override suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey) = Unit
}
