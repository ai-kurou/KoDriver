package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData

internal data class LmuWindowsReadoutBrakeWearDetailUiState(
    /** ブレーキ残量。値を取得できていない間は null。 */
    val remaining: LmuWindowsBrakeWearRemainingData? = null,
)
