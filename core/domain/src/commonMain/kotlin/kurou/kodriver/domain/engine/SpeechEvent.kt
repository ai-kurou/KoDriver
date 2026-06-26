package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

sealed interface SpeechEvent {
    val readoutItemKey: ReadoutItemKey

    data object CarLeft : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VehicleApproach }
    data object CarRight : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VehicleApproach }
    data object LeftApproach : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VehicleApproach }
    data object RightApproach : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VehicleApproach }
    data object BlueFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.Flag }
    data object YellowFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.Flag }
    data object FullCourseYellow : SpeechEvent { override val readoutItemKey = ReadoutItemKey.Flag }
    data object SessionStop : SpeechEvent { override val readoutItemKey = ReadoutItemKey.Flag }
    data object Overheating : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VehicleDamage }
    data object MyBestLapFormal : SpeechEvent { override val readoutItemKey = ReadoutItemKey.MyBestLap }
    data object MyBestLapCasual : SpeechEvent { override val readoutItemKey = ReadoutItemKey.MyBestLap }
    data class RemainingFuelLapsWarning(val laps: Int) : SpeechEvent {
        override val readoutItemKey = ReadoutItemKey.RemainingFuelLaps
    }
}
