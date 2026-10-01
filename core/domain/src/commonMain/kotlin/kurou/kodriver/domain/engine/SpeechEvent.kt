package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

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
     * WAVイベントでは収録音声・チップ表示と一致し、LMUフラッグでは自由文字列の既定値に一致する。
     * ドメイン層はCompose Resourcesに依存しないため、表示文言の変更時はここも更新する。
     */
    val narratedText: String

    data object CarLeft : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "カーレフト"
    }

    data object CarRight : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "カーライト"
    }

    data object LeftApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "左接近"
    }

    data object RightApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "右接近"
    }

    data object KeepLeft : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "キープレフト"
    }

    data object KeepRight : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "キープライト"
    }

    data object LeftSustained : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "左側維持"
    }

    data object RightSustained : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach.Root
        override val narratedText = "右側維持"
    }

    data object BlueFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = "ブルーフラッグ"
    }

    data object YellowFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = "イエローフラッグ"
    }

    data object FullCourseYellow : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = "フルコースイエロー"
    }

    data object RedFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root
        override val narratedText = "レッドフラッグ"
    }

    data object Overheating : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = "GP2 GP2… ahhh!!!"
    }

    data object OverheatingStandard : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = "オーバーヒート"
    }

    data object PartDetached : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = "部品脱落"
    }

    data object TyreDetached : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root
        override val narratedText = "タイヤ脱落"
    }

    data object TyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
        override val narratedText = "タイヤ過熱警告"
    }

    data object TyreCold : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature.Root
        override val narratedText = "タイヤ低温警告"
    }

    data object TyreWearWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreWear.Root
        override val narratedText = "タイヤ摩耗警告"
    }

    data object BrakeOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.BrakeTemperature.Root
        override val narratedText = "ブレーキ過熱警告"
    }

    data object RemainingVirtualEnergyWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root
        override val narratedText = "バーチャルエナジー残量警告"
    }

    data object LmuWindowsMyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root
        override val narratedText = "自己ベストラップ更新"
    }

    data object LmuWindowsMyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap.Root
        override val narratedText = "ベストラップ"
    }

    data object Gt7Ps5MyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        override val narratedText = "自己ベストラップ更新"
    }

    data object Gt7Ps5MyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        override val narratedText = "ベストラップ"
    }

    /** GT7 の燃料残量から推定した残り周回数を読み上げるイベント。 */
    data class RemainingFuelLapsWarning(
        val laps: Int,
    ) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
        override val narratedText = if (laps <= 0) "燃料がありません" else "燃料は残り約${laps}周"
    }

    data object Gt7Ps5RemainingFuelWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root
        override val narratedText = "残り燃料警告"
    }

    data object Gt7Ps5TyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.TyreTemperature.Root
        override val narratedText = "タイヤ過熱警告"
    }

    /** LMU のバーチャルエナジーまたはタイヤ摩耗から推定したピット目安周回数を読み上げるイベント。 */
    data class PitTimingWarning(
        val laps: Int,
    ) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.PitTiming.Root
        override val narratedText = if (laps <= 0) "必ずピットイン" else "残り約${laps}周でピットイン"
    }

    /** ACE の残燃料で走行可能な周回数を読み上げるイベント。文言・WAV は GT7 の [RemainingFuelLapsWarning] と共通。 */
    data class AceWindowsRemainingFuelLapsWarning(
        val laps: Int,
    ) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuelLaps.Root
        override val narratedText = if (laps <= 0) "燃料がありません" else "燃料は残り約${laps}周"
    }

    data object AceWindowsRemainingFuelWarning : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.RemainingFuel.Root
        override val narratedText = "残り燃料警告"
    }

    data object AceWindowsWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "ホワイトフラッグ"
    }

    data object AceWindowsGreenFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "グリーンフラッグ"
    }

    data object AceWindowsRedFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "レッドフラッグ"
    }

    data object AceWindowsBlueFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "ブルーフラッグ"
    }

    data object AceWindowsYellowFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "イエローフラッグ"
    }

    data object AceWindowsBlackFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "ブラックフラッグ"
    }

    data object AceWindowsBlackWhiteFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "ブラック・ホワイトフラッグ"
    }

    data object AceWindowsCheckeredFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "チェッカーフラッグ"
    }

    data object AceWindowsOrangeCircleFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "オレンジボールフラッグ"
    }

    data object AceWindowsRedYellowStripesFlag : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.Flag.Root
        override val narratedText = "レッド・イエローストライプフラッグ"
    }

    data object AceWindowsTyreOverheat : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.TyreTemperature.Root
        override val narratedText = "タイヤ過熱警告"
    }

    /**
     * ACE の周辺車両接近を読み上げるイベント。
     *
     * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、LMU（[CarLeft]/[CarRight] 等）のような
     * 左右を区別した接近アナウンスができないため、左右を区別しない汎用の接近アナウンスとして1種類のみ用意する。
     */
    data object AceWindowsVehicleApproach : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.AceWindows.VehicleApproach.Root
        override val narratedText = "車両接近"
    }

    /**
     * ACE の自己ベストラップ更新を読み上げるイベント（フォーマル / カジュアルの2種）。
     *
     * 再生する WAV は LMU（[LmuWindowsMyBestLapFormal]/[LmuWindowsMyBestLapCasual]）と同じ音源を流用する。
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
