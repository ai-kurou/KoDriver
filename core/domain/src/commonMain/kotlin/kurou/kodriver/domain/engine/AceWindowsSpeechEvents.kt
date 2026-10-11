package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.formatAceWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.model.formatAceWindowsTyreTemperatureReadoutText

/** ACE の残燃料で走行可能な周回数。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class AceWindowsRemainingFuelLapsWarning(
    val laps: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.RemainingFuelLaps.Root
    override val narratedText =
        if (laps <= 0) {
            ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
        } else {
            formatAceWindowsRemainingFuelLapsReadoutText(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, laps)
        }

    override fun withResolvedText(text: String): AceWindowsRemainingFuelLapsWarning = copy(resolvedText = text)
}

/**
 * percent は判定時の実残量を四捨五入した整数。
 * resolvedText は判定時に解決済みの文言で、キュー待機中に設定が変わってもログと発話を一致させる。
 */
data class AceWindowsRemainingFuelWarning(
    val percent: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.RemainingFuel.Root
    override val narratedText =
        formatAceWindowsRemainingFuelReadoutText(ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT, percent)

    override fun withResolvedText(text: String): AceWindowsRemainingFuelWarning = copy(resolvedText = text)
}

data class AceWindowsWhiteFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsWhiteFlag = copy(resolvedText = text)
}

data class AceWindowsGreenFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsGreenFlag = copy(resolvedText = text)
}

data class AceWindowsRedFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsRedFlag = copy(resolvedText = text)
}

data class AceWindowsBlueFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsBlueFlag = copy(resolvedText = text)
}

data class AceWindowsYellowFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsYellowFlag = copy(resolvedText = text)
}

data class AceWindowsBlackFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsBlackFlag = copy(resolvedText = text)
}

data class AceWindowsBlackWhiteFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsBlackWhiteFlag = copy(resolvedText = text)
}

data class AceWindowsCheckeredFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsCheckeredFlag = copy(resolvedText = text)
}

data class AceWindowsOrangeCircleFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsOrangeCircleFlag = copy(resolvedText = text)
}

data class AceWindowsRedYellowStripesFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.Flag.Root
    override val narratedText = ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsRedYellowStripesFlag = copy(resolvedText = text)
}

/** 判定時点の最大カーカス温度と解決済み本文を保持するタイヤ過熱イベント。 */
data class AceWindowsTyreOverheat(
    val celsius: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        formatAceWindowsTyreTemperatureReadoutText(
            ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
            celsius,
        )

    override fun withResolvedText(text: String): AceWindowsTyreOverheat = copy(resolvedText = text)
}

/**
 * ACE の周辺車両接近を読み上げるイベント。
 *
 * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、LMU（[LmuWindowsCarLeft]/[LmuWindowsCarRight] 等）のような
 * 左右を区別した接近アナウンスができないため、左右を区別しない汎用の接近アナウンスとして1種類のみ用意する。
 */
data class AceWindowsVehicleApproach(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.VehicleApproach.Root
    override val narratedText = ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): AceWindowsVehicleApproach = copy(resolvedText = text)
}

/** ACE の更新後の自己ベストタイム。解決済み文言は判定時の発話・ログ内容を保持する。 */
data class AceWindowsMyBestLap(
    val lapTimeMs: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = AceWindowsReadoutItemKey.MyBestLap.Root
    override val narratedText =
        formatAceWindowsMyBestLapReadoutText(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, lapTimeMs)

    override fun withResolvedText(text: String): AceWindowsMyBestLap = copy(resolvedText = text)
}
