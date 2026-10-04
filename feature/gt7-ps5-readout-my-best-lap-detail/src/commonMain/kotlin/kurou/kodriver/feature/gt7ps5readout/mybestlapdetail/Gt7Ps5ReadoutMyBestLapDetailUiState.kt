package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT

internal data class Gt7Ps5ReadoutMyBestLapDetailUiState(
    val readoutText: String = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
