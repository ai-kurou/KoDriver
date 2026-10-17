package kurou.kodriver.feature.gt7ps5autoreadout.mybestlapdetail

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT

internal data class Gt7Ps5AutoReadoutMyBestLapDetailUiState(
    val readoutText: String = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
