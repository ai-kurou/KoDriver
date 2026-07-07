package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

sealed interface SpeechEvent {
    val readoutItemKey: ReadoutItemKey

    data object CarLeft : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach }
    data object CarRight : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach }
    data object LeftApproach : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach }
    data object RightApproach : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleApproach }
    data object BlueFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object YellowFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object FullCourseYellow : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object SessionStop : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object Overheating : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root }
    data object TyreOverheat : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.TyreTemperature }
    data object LmuWindowsMyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap
    }
    data object LmuWindowsMyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.LmuWindows.MyBestLap
    }
    data object Gt7Ps5MyBestLapFormal : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap
    }
    data object Gt7Ps5MyBestLapCasual : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.MyBestLap
    }
    data class RemainingFuelLapsWarning(val laps: Int) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps
    }
}
