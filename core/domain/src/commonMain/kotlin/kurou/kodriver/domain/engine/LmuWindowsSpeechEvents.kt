package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.defaultLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText

data class LmuWindowsCarLeft(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleApproach.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsCarLeft = copy(resolvedText = text)
}

data class LmuWindowsCarRight(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleApproach.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsCarRight = copy(resolvedText = text)
}

data class LmuWindowsCarLeftSustained(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleApproach.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsCarLeftSustained = copy(resolvedText = text)
}

data class LmuWindowsCarRightSustained(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleApproach.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsCarRightSustained = copy(resolvedText = text)
}

data class LmuWindowsBlueFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root
    override val narratedText = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsBlueFlag = copy(resolvedText = text)
}

data class LmuWindowsYellowFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root
    override val narratedText = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsYellowFlag = copy(resolvedText = text)
}

data class LmuWindowsFullCourseYellow(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root
    override val narratedText = LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsFullCourseYellow = copy(resolvedText = text)
}

data class LmuWindowsRedFlag(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root
    override val narratedText = LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsRedFlag = copy(resolvedText = text)
}

data class LmuWindowsOverheating(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleDamage.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsOverheating = copy(resolvedText = text)
}

data class LmuWindowsPartDetached(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleDamage.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsPartDetached = copy(resolvedText = text)
}

data class LmuWindowsTyreDetached(
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.VehicleDamage.Root
    override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT

    override fun withResolvedText(text: String): LmuWindowsTyreDetached = copy(resolvedText = text)
}

/**
 * タイヤ過熱警告。[celsius] は判定時の全輪の最高カーカス温度を整数に丸めた摂氏温度。
 * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
 */
data class LmuWindowsTyreOverheat(
    val celsius: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        formatLmuWindowsTyreTemperatureReadoutText(
            LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
            celsius,
        )

    override fun withResolvedText(text: String): LmuWindowsTyreOverheat = copy(resolvedText = text)
}

/**
 * タイヤ低温警告。[celsius] は判定時の全輪の最高カーカス温度を整数に丸めた摂氏温度。
 * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
 */
data class LmuWindowsTyreCold(
    val celsius: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.TyreTemperature.Root
    override val narratedText =
        formatLmuWindowsTyreTemperatureReadoutText(
            LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
            celsius,
        )

    override fun withResolvedText(text: String): LmuWindowsTyreCold = copy(resolvedText = text)
}

/**
 * タイヤ摩耗警告。[percentage] は実際の残存率ではなく設定した閾値（%）。
 * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わっても、ログと発話内容を一致させるために使う。
 */
data class LmuWindowsTyreWearWarning(
    val percentage: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.TyreWear.Root
    override val narratedText =
        formatLmuWindowsTyreWearReadoutText(
            LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
            percentage,
        )

    override fun withResolvedText(text: String): LmuWindowsTyreWearWarning = copy(resolvedText = text)
}

/**
 * ブレーキ過熱警告。[celsius] は実測ではなく設定した閾値（℃）。
 * [resolvedText] は判定時に解決済みの文言。キュー待機中もログと発話を一致させる。
 */
data class LmuWindowsBrakeOverheat(
    val celsius: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.BrakeTemperature.Root
    override val narratedText =
        formatLmuWindowsBrakeTemperatureReadoutText(
            LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
            celsius,
        )

    override fun withResolvedText(text: String): LmuWindowsBrakeOverheat = copy(resolvedText = text)
}

/**
 * ブレーキ残量警告。[percent] は実測の残量ではなく設定した閾値（%）。
 * [resolvedText] は判定時に解決済みの文言。キュー待機中もログと発話を一致させる。
 */
data class LmuWindowsBrakeWearLow(
    val percent: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.BrakeWear.Root
    override val narratedText =
        formatLmuWindowsBrakeWearReadoutText(
            LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
            percent,
        )

    override fun withResolvedText(text: String): LmuWindowsBrakeWearLow = copy(resolvedText = text)
}

/**
 * バーチャルエナジー残量警告。[percentage] は実際の残量ではなく設定した閾値（%）。
 * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わっても、ログと発話内容を一致させるために使う。
 */
data class LmuWindowsRemainingVirtualEnergyWarning(
    val percentage: Int,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
    override val narratedText =
        formatLmuWindowsRemainingVirtualEnergyReadoutText(
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
            percentage,
        )

    override fun withResolvedText(text: String): LmuWindowsRemainingVirtualEnergyWarning = copy(resolvedText = text)
}

data class LmuWindowsMyBestLap(
    val lapTimeMs: Long,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.MyBestLap.Root
    override val narratedText =
        formatLmuWindowsMyBestLapReadoutText(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, lapTimeMs)

    override fun withResolvedText(text: String): LmuWindowsMyBestLap = copy(resolvedText = text)
}

/**
 * LMU のバーチャルエナジーまたはタイヤ摩耗から推定したピット目安周回数を読み上げるイベント。
 * [narratedText] はログ用の既定文言。実際の読み上げ文言はソースごとの設定値から取得する。
 */
data class LmuWindowsPitTimingWarning(
    val laps: Int,
    val source: PitTimingSource,
    override val resolvedText: String? = null,
) : ReadoutTextEvent {
    override val readoutItemKey = LmuWindowsReadoutItemKey.PitTiming.Root
    override val narratedText = defaultLmuWindowsPitTimingReadoutText(source, laps)

    override fun withResolvedText(text: String): LmuWindowsPitTimingWarning = copy(resolvedText = text)
}
