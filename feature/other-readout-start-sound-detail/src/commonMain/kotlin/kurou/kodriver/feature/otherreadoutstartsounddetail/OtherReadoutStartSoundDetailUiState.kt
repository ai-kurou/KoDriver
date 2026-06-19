package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.model.ReadoutStartSoundType

data class OtherReadoutStartSoundDetailUiState(
    val selectedType: ReadoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
    val pendingType: ReadoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
)
