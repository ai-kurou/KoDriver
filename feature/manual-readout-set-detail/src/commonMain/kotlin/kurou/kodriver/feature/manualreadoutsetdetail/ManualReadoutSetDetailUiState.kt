package kurou.kodriver.feature.manualreadoutsetdetail

import kurou.kodriver.domain.model.HotkeyRegistrationState

internal data class ManualReadoutSetDetailUiState(
    val setNumber: Int? = null,
    val availableItemKeys: List<String> = emptyList(),
    val selectedItemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
    val hotkeyVirtualKeyCode: Int? = null,
    val hotkeyRegistrationState: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
    val hotkeyConflict: ManualReadoutHotkeyConflict? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false,
)
