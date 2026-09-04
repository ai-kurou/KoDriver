package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.model.OVERHEAT_VOICE_TYPE_DEFAULT
import kurou.kodriver.domain.model.OverheatVoiceType

internal data class LmuWindowsReadoutVehicleDamageDetailUiState(
    val overheatEnabled: Boolean = true,
    val overheatVoiceType: OverheatVoiceType = OVERHEAT_VOICE_TYPE_DEFAULT,
    val partDetachedEnabled: Boolean = true,
)
