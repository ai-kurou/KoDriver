package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

internal data class ManualReadoutSetDetailUiState(
    val setNumber: Int? = null,
    val availableItemKeys: List<String> = emptyList(),
    val selectedItemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
    val hotkeyModifiers: ManualReadoutHotkeyModifiers = MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT,
    val hotkeyVirtualKeyCode: Int? = null,
    val hotkeyRegistrationState: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
    val hotkeyConflict: ManualReadoutHotkeyConflict? = null,
    val isTextToSpeechAvailable: Boolean = false,
    val isPreviewing: Boolean = false,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
)
