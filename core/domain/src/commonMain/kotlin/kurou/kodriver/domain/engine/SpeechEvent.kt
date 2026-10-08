package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
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
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
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
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.defaultLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText

/**
 * 音声エンジンへ渡す読み上げイベント。
 *
 * 各イベントは、WAVまたはOS標準TTSによる読み上げの種類と、読み上げ可否を判定する
 * [ReadoutItemKey] を結び付ける。キューイング可否はイベント単位ではなく
 * [readoutItemKey] のトップレベル項目で判定する。
 */
sealed interface SpeechEvent {
    /** このイベントを有効化・キュー可否判定に関連付ける読み上げ項目。 */
    val readoutItemKey: ReadoutItemKey

    /**
     * テレメトリログに記録するイベントの既定文言。
     * WAVイベントでは収録音声・チップ表示と一致する。LMUの自由文字列イベントでは既定文言を参照し、
     * 判定時の実際の本文は [FreeTextSpeechEvent.resolvedText] に保持する。
     * ACEフラッグは自由文字列を読み上げるため、実際の本文ではなく既定文言の定数を参照する。
     * ドメイン層はCompose Resourcesに依存しないため、表示文言の変更時はここも更新する。
     */
    val narratedText: String

    data class CarLeft(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): CarLeft = copy(resolvedText = text)
    }

    data class CarRight(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): CarRight = copy(resolvedText = text)
    }

    data class CarLeftSustained(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): CarLeftSustained = copy(resolvedText = text)
    }

    data class CarRightSustained(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): CarRightSustained = copy(resolvedText = text)
    }

    data class BlueFlag(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): BlueFlag = copy(resolvedText = text)
    }

    data class YellowFlag(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): YellowFlag = copy(resolvedText = text)
    }

    data class FullCourseYellow(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): FullCourseYellow = copy(resolvedText = text)
    }

    data class RedFlag(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): RedFlag = copy(resolvedText = text)
    }

    data class Overheating(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): Overheating = copy(resolvedText = text)
    }

    data class PartDetached(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): PartDetached = copy(resolvedText = text)
    }

    data class TyreDetached(
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT

        override fun withResolvedText(text: String): TyreDetached = copy(resolvedText = text)
    }

    /**
     * タイヤ過熱警告。[celsius] は判定時の全輪の最高カーカス温度を整数に丸めた摂氏温度。
     * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
     */
    data class TyreOverheat(
        val celsius: Int,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
        override val narratedText =
            formatLmuWindowsTyreTemperatureReadoutText(
                LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                celsius,
            )

        override fun withResolvedText(text: String): TyreOverheat = copy(resolvedText = text)
    }

    /**
     * タイヤ低温警告。[celsius] は判定時の全輪の最高カーカス温度を整数に丸めた摂氏温度。
     * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わってもログと発話内容を一致させる。
     */
    data class TyreCold(
        val celsius: Int,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
        override val narratedText =
            formatLmuWindowsTyreTemperatureReadoutText(
                LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
                celsius,
            )

        override fun withResolvedText(text: String): TyreCold = copy(resolvedText = text)
    }

    /**
     * タイヤ摩耗警告。[percentage] は実際の残存率ではなく設定した閾値（%）。
     * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わっても、ログと発話内容を一致させるために使う。
     */
    data class TyreWearWarning(
        val percentage: Int,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root
        override val narratedText =
            formatLmuWindowsTyreWearReadoutText(
                LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                percentage,
            )

        override fun withResolvedText(text: String): TyreWearWarning = copy(resolvedText = text)
    }

    /**
     * ブレーキ過熱警告。[celsius] は実測ではなく設定した閾値（℃）。
     * [resolvedText] は判定時に解決済みの文言。キュー待機中もログと発話を一致させる。
     */
    data class BrakeOverheat(
        val celsius: Int,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root
        override val narratedText =
            formatLmuWindowsBrakeTemperatureReadoutText(
                LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
                celsius,
            )

        override fun withResolvedText(text: String): BrakeOverheat = copy(resolvedText = text)
    }

    /**
     * バーチャルエナジー残量警告。[percentage] は実際の残量ではなく設定した閾値（%）。
     * [resolvedText] は判定時に解決済みの読み上げ文言。キュー待機中に設定が変わっても、ログと発話内容を一致させるために使う。
     */
    data class RemainingVirtualEnergyWarning(
        val percentage: Int,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root
        override val narratedText =
            formatLmuWindowsRemainingVirtualEnergyReadoutText(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
                percentage,
            )

        override fun withResolvedText(text: String): RemainingVirtualEnergyWarning = copy(resolvedText = text)
    }

    data class LmuWindowsMyBestLap(
        val lapTimeMs: Long,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root
        override val narratedText =
            formatLmuWindowsMyBestLapReadoutText(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, lapTimeMs)

        override fun withResolvedText(text: String): LmuWindowsMyBestLap = copy(resolvedText = text)
    }

    /** GT7 の更新後の自己ベストタイム。解決済み文言は判定時の発話・ログ内容を保持する。 */
    data class Gt7Ps5MyBestLap(
        val lapTimeMs: Int,
        override val resolvedText: String? = null,
    ) : Gt7Ps5ReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
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
    ) : Gt7Ps5ReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
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
    ) : Gt7Ps5ReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root
        override val narratedText =
            formatGt7Ps5RemainingFuelReadoutText(GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT, percent)

        override fun withResolvedText(text: String): Gt7Ps5RemainingFuelWarning = copy(resolvedText = text)
    }

    /** GT7 の全輪の最高タイヤ温度。解決済み文言は判定時の発話・ログ内容を保持する。 */
    data class Gt7Ps5TyreOverheat(
        val celsius: Int,
        override val resolvedText: String? = null,
    ) : Gt7Ps5ReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.TyreTemperature.Root
        override val narratedText =
            formatGt7Ps5TyreTemperatureReadoutText(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, celsius)

        override fun withResolvedText(text: String): Gt7Ps5TyreOverheat = copy(resolvedText = text)
    }

    /**
     * LMU のバーチャルエナジーまたはタイヤ摩耗から推定したピット目安周回数を読み上げるイベント。
     * [narratedText] はログ用の既定文言。実際の読み上げ文言はソースごとの設定値から取得する。
     */
    data class PitTimingWarning(
        val laps: Int,
        val source: PitTimingSource,
        override val resolvedText: String? = null,
    ) : FreeTextSpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root
        override val narratedText = defaultLmuWindowsPitTimingReadoutText(source, laps)

        override fun withResolvedText(text: String): PitTimingWarning = copy(resolvedText = text)
    }

    /** ACE の残燃料で走行可能な周回数。解決済み文言は判定時の発話・ログ内容を保持する。 */
    data class AceWindowsRemainingFuelLapsWarning(
        val laps: Int,
        override val resolvedText: String? = null,
    ) : AceWindowsReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuelLaps.Root
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
    ) : AceWindowsReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuel.Root
        override val narratedText =
            formatAceWindowsRemainingFuelReadoutText(ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT, percent)

        override fun withResolvedText(text: String): AceWindowsRemainingFuelWarning = copy(resolvedText = text)
    }

    data object AceWindowsWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsGreenFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsRedFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsBlueFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsYellowFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsBlackFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsBlackWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsCheckeredFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsOrangeCircleFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
    }

    data object AceWindowsRedYellowStripesFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
    }

    /** 判定時点の最大カーカス温度と解決済み本文を保持するタイヤ過熱イベント。 */
    data class AceWindowsTyreOverheat(
        val celsius: Int,
        override val resolvedText: String? = null,
    ) : AceWindowsReadoutTextEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.TyreTemperature.Root
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
     * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、LMU（[CarLeft]/[CarRight] 等）のような
     * 左右を区別した接近アナウンスができないため、左右を区別しない汎用の接近アナウンスとして1種類のみ用意する。
     */
    data object AceWindowsVehicleApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.VehicleApproach.Root
        override val narratedText = ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
    }

    /**
     * ACE の自己ベストラップ更新を読み上げるイベント（フォーマル / カジュアルの2種）。
     *
     * ACE専用の WAV 音源を再生する。
     */
    data object AceWindowsMyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.MyBestLap.Root
        override val narratedText = "自己ベストラップ更新"
    }

    data object AceWindowsMyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.MyBestLap.Root
        override val narratedText = "ベストラップ"
    }
}

/** 判定時の本文を保持し、キュー待機中の設定変更後も発話とログを一致させるLMUイベント。 */
sealed interface FreeTextSpeechEvent : SpeechEvent {
    val resolvedText: String?

    fun withResolvedText(text: String): FreeTextSpeechEvent
}

/** GT7の自由文言イベント。文言取得の分岐を網羅し、判定時の本文を発話・ログで共有する。 */
sealed interface Gt7Ps5ReadoutTextEvent : SpeechEvent {
    val resolvedText: String?

    fun withResolvedText(text: String): Gt7Ps5ReadoutTextEvent
}

/** ACEの自由文言イベント。判定時の本文を発話・ログで共有する。 */
sealed interface AceWindowsReadoutTextEvent : SpeechEvent {
    val resolvedText: String?

    fun withResolvedText(text: String): AceWindowsReadoutTextEvent
}
