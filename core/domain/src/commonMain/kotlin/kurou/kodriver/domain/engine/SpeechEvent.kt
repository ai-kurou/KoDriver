package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 音声エンジンへ渡す読み上げイベント。
 *
 * 各イベントは、実際に再生される WAV ファイルの種類と、読み上げ可否を判定する
 * [ReadoutItemKey] を結び付ける。キューイング可否はイベント単位ではなく
 * [readoutItemKey] のトップレベル項目で判定する。
 */
sealed interface SpeechEvent {
    /** このイベントを有効化・キュー可否判定に関連付ける読み上げ項目。 */
    val readoutItemKey: ReadoutItemKey

    data object CarLeft : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object CarRight : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object LeftApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object RightApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object KeepLeft : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object KeepRight : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object LeftSustained : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object RightSustained : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
    }

    data object BlueFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
    }

    data object YellowFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
    }

    data object FullCourseYellow : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
    }

    data object SessionStop : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
    }

    data object RedFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
    }

    data object Overheating : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
    }

    data object TyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
    }

    data object TyreCold : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
    }

    data object TyreWearWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root
    }

    data object RemainingVirtualEnergyWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root
    }

    data object LmuWindowsMyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root
    }

    data object LmuWindowsMyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root
    }

    data object Gt7Ps5MyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
    }

    data object Gt7Ps5MyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
    }

    /** GT7 の燃料残量から推定した残り周回数を読み上げるイベント。 */
    data class RemainingFuelLapsWarning(
        val laps: Int,
    ) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
    }

    data object Gt7Ps5RemainingFuelWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root
    }

    data object Gt7Ps5TyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.TyreTemperature.Root
    }

    /** LMU のバーチャルエナジーまたはタイヤ摩耗から推定したピット目安周回数を読み上げるイベント。 */
    data class PitTimingWarning(
        val laps: Int,
    ) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root
    }

    data object AceWindowsRemainingFuelWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuel.Root
    }

    data object AceWindowsWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsGreenFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsRedFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsBlueFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsYellowFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsBlackFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsBlackWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsCheckeredFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsOrangeCircleFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsRedYellowStripesFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
    }

    data object AceWindowsTyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.TyreTemperature.Root
    }

    /**
     * ACE の周辺車両接近を読み上げるイベント。
     *
     * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、LMU（[CarLeft]/[CarRight] 等）のような
     * 左右を区別した接近アナウンスができないため、左右を区別しない汎用の接近アナウンスとして1種類のみ用意する。
     */
    data object AceWindowsVehicleApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.VehicleApproach.Root
    }

    /**
     * ACE の自己ベストラップ更新を読み上げるイベント（フォーマル / カジュアルの2種）。
     *
     * 再生する WAV は LMU（[LmuWindowsMyBestLapFormal]/[LmuWindowsMyBestLapCasual]）と同じ音源を流用する。
     */
    data object AceWindowsMyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.MyBestLap.Root
    }

    data object AceWindowsMyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.MyBestLap.Root
    }
}
