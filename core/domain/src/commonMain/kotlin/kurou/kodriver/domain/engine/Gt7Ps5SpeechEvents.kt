package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5TyreTemperatureReadoutText

/** GT7 の更新後の自己ベストタイム。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class Gt7Ps5MyBestLap(
    val lapTimeMs: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root
    override val narratedText =
        formatGt7Ps5MyBestLapReadoutText(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT, lapTimeMs)

    override fun withResolvedText(text: String): Gt7Ps5MyBestLap = copy(resolvedText = text)
}

/**
 * GT7 の燃料残量から推定した残り周回数を読み上げるイベント。
 * [resolvedText] は判定時に解決済みの文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
 */
data class Gt7Ps5RemainingFuelLapsWarning(
    val laps: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
    override val narratedText =
        if (laps <= 0) {
            GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
        } else {
            formatGt7Ps5RemainingFuelLapsReadoutText(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, laps)
        }

    override fun withResolvedText(text: String): Gt7Ps5RemainingFuelLapsWarning = copy(resolvedText = text)
}

/**
 * GT7 の実際の燃料残量を四捨五入した整数 [percent] で読み上げるイベント。
 * [resolvedText] は判定時に解決済みの文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
 */
data class Gt7Ps5RemainingFuelWarning(
    val percent: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
    override val narratedText =
        formatGt7Ps5RemainingFuelReadoutText(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT, percent)

    override fun withResolvedText(text: String): Gt7Ps5RemainingFuelWarning = copy(resolvedText = text)
}

/** GT7 の全輪の最高タイヤ温度。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class Gt7Ps5TyreOverheat(
    val celsius: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        formatGt7Ps5TyreTemperatureReadoutText(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, celsius)

    override fun withResolvedText(text: String): Gt7Ps5TyreOverheat = copy(resolvedText = text)
}
