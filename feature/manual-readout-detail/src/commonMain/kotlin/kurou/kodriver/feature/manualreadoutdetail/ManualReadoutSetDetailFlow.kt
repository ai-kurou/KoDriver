package kurou.kodriver.feature.manualreadoutdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet

/** 手動読み上げセット詳細と画面内状態を合成する冷たい Flow。 */
@Suppress("LongParameterList")
internal class ManualReadoutSetDetailFlow(
    sets: Flow<List<ManualReadoutSet>>,
    deleted: Flow<Boolean>,
    hotkeyConflict: Flow<ManualReadoutHotkeyConflict?>,
    debugHotkeyVirtualKeyCode: Flow<Int?>,
    hotkeyRegistrationState: Flow<HotkeyRegistrationState>,
    textToSpeechAvailable: Flow<Boolean>,
    isPreviewing: Flow<Boolean>,
    private val simulatorId: String,
    private val setId: String,
    private val availableItemKeys: List<String>,
) {
    val initialUiState =
        ManualReadoutSetDetailUiState(simulatorId = simulatorId, availableItemKeys = availableItemKeys)

    val uiState: Flow<ManualReadoutSetDetailUiState> =
        combine(sets, deleted, hotkeyConflict, debugHotkeyVirtualKeyCode, hotkeyRegistrationState) {
            sets,
            isDeleted,
            conflict,
            debugVirtualKeyCode,
            registration,
            ->
            val index = sets.indexOfFirst { it.id == setId }
            val set = sets.getOrNull(index)
            val hotkey = set?.hotkeyVirtualKeyCode
            val modifiers = set?.hotkeyModifiers ?: MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
            // 保存後にデバッグ用ホットキーが同じキーへ変更された場合も警告する。
            val savedConflict =
                ManualReadoutHotkeyConflict.DebugHotkey.takeIf {
                    modifiers == ManualReadoutHotkeyModifiers.CTRL_ALT_SHIFT &&
                        hotkey != null && hotkey == debugVirtualKeyCode
                }
            ManualReadoutSetDetailUiState(
                simulatorId = simulatorId,
                setNumber = if (index >= 0) sets[index].number.takeIf { it > 0 } ?: (index + 1) else null,
                name = set?.name.orEmpty(),
                availableItemKeys = availableItemKeys,
                selectedItemKeys = set?.itemKeys.orEmpty(),
                itemTexts = set?.itemTexts.orEmpty(),
                hotkeyVirtualKeyCode = hotkey,
                hotkeyModifiers = modifiers,
                hotkeyRegistrationState = registration,
                hotkeyConflict = conflict ?: savedConflict,
                isLoading = false,
                deleted = isDeleted,
            )
        }.combine(textToSpeechAvailable) { state, available ->
            state.copy(isTextToSpeechAvailable = available)
        }.combine(isPreviewing) { state, previewing ->
            state.copy(isPreviewing = previewing)
        }
}
