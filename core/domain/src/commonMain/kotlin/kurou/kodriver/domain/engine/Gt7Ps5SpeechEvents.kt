package kurou.kodriver.domain.engine

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey

/** GT7 由来の読み上げイベントを識別する共通マーカー。自由文言経路で本文を扱うため [ReadoutTextEvent] の契約を持つ。 */
sealed interface Gt7Ps5SpeechEvent : ReadoutTextEvent

/** GT7 の更新後の自己ベストタイム。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class Gt7Ps5MyBestLap(
    val lapTimeMs: Int,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
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
) : Gt7Ps5SpeechEvent {
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
) : Gt7Ps5SpeechEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
    override val narratedText =
        formatGt7Ps5RemainingFuelReadoutText(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT, percent)

    override fun withResolvedText(text: String): Gt7Ps5RemainingFuelWarning = copy(resolvedText = text)
}

/** GT7 の全輪の最高タイヤ温度。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class Gt7Ps5TyreOverheat(
    val celsius: Int,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        formatGt7Ps5TyreTemperatureReadoutText(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, celsius)

    override fun withResolvedText(text: String): Gt7Ps5TyreOverheat = copy(resolvedText = text)
}

/** セット内の項目を保存順に連結する手動読み上げイベント。 */
data class Gt7Ps5ManualReadoutText(
    override val readoutItemKey: Gt7Ps5ReadoutItemKey,
    val text: String,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
    override val narratedText = resolvedText ?: text

    override fun withResolvedText(text: String): Gt7Ps5ManualReadoutText = copy(resolvedText = text)
}

/** GT7 の現在の燃料残量。手動読み上げではセット内の文言を優先する。 */
data class Gt7Ps5RemainingFuelCurrentValue(
    val percent: Int,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuel.Root
    override val narratedText =
        resolvedText ?: formatGt7Ps5RemainingFuelReadoutText(
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
            percent,
        )

    override fun withResolvedText(text: String): Gt7Ps5RemainingFuelCurrentValue = copy(resolvedText = text)
}

/** GT7 の現在の残り燃料周回数。手動読み上げではセット内の文言を優先する。 */
data class Gt7Ps5RemainingFuelLapsCurrentValue(
    val laps: Int,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root
    override val narratedText =
        resolvedText ?: if (laps <= 0) {
            GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
        } else {
            formatGt7Ps5RemainingFuelLapsReadoutText(GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT, laps)
        }

    override fun withResolvedText(text: String): Gt7Ps5RemainingFuelLapsCurrentValue = copy(resolvedText = text)
}

/** GT7 の現在の最高タイヤ温度。手動読み上げではセット内の文言を優先する。 */
data class Gt7Ps5TyreTemperatureCurrentValue(
    val celsius: Int,
    override val resolvedText: String? = null,
) : Gt7Ps5SpeechEvent {
    override val readoutItemKey = Gt7Ps5ReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        resolvedText ?: formatGt7Ps5TyreTemperatureReadoutText(
            GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
            celsius,
        )

    override fun withResolvedText(text: String): Gt7Ps5TyreTemperatureCurrentValue = copy(resolvedText = text)
}
