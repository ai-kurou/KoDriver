package kurou.kodriver.feature.manualreadoutdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutSet

/** 手動読み上げセット詳細と画面内状態を合成する冷たい Flow。 */
@Suppress("LongParameterList")
internal class ManualReadoutSetDetailFlow(
    sets: Flow<List<ManualReadoutSet>>,
    deleted: Flow<Boolean>,
    hotkeyConflict: Flow<ManualReadoutHotkeyConflict?>,
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
        combine(sets, deleted, hotkeyConflict, hotkeyRegistrationState) {
            sets,
            isDeleted,
            conflict,
            registration,
            ->
            val index = sets.indexOfFirst { it.id == setId }
            val set = sets.getOrNull(index)
            val hotkey = set?.hotkeyVirtualKeyCode
            val modifiers = set?.hotkeyModifiers ?: MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
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
                hotkeyConflict = conflict,
                isLoading = false,
                deleted = isDeleted,
            )
        }.combine(textToSpeechAvailable) { state, available ->
            state.copy(isTextToSpeechAvailable = available)
        }.combine(isPreviewing) { state, previewing ->
            state.copy(isPreviewing = previewing)
        }
}
