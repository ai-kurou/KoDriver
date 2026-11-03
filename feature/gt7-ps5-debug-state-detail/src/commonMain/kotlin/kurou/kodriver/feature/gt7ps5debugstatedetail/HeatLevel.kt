package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.core.designsystem.temperatureHeatLevel
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

// 読み上げ判定と同じ GT7 の既定閾値を使う。
internal fun tyreTemperatureHeatLevel(celsius: Double): HeatLevel =
    temperatureHeatLevel(
        celsius = celsius,
        warmThreshold = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toDouble(),
    )
