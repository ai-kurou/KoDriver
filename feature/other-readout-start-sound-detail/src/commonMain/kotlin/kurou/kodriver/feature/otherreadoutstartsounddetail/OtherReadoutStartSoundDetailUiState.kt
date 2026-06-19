package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.model.ReadoutStartSoundType

data class OtherReadoutStartSoundDetailUiState(
    val selectedType: ReadoutStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
    val pendingType: ReadoutStartSoundType = ReadoutStartSoundType.FORMULA_RADIO,
)
