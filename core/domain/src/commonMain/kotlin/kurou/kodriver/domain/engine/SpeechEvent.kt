package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

sealed interface SpeechEvent {
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
    data object BlueFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object YellowFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object FullCourseYellow : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object SessionStop : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object RedFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root }
    data object Overheating : SpeechEvent { override val readoutItemKey = ReadoutItemKey.LmuWindows.VehicleDamage.Root }
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
    data class RemainingFuelLapsWarning(val laps: Int) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
    }
}
