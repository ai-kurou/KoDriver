package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

sealed interface SpeechEvent {
    val readoutItemKey: String

    data object CarLeft : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VEHICLE_APPROACH }
    data object CarRight : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VEHICLE_APPROACH }
    data object BlueFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.FLAG }
    data object YellowFlag : SpeechEvent { override val readoutItemKey = ReadoutItemKey.FLAG }
    data object FullCourseYellow : SpeechEvent { override val readoutItemKey = ReadoutItemKey.FLAG }
    data object SessionStop : SpeechEvent { override val readoutItemKey = ReadoutItemKey.FLAG }
    data object Overheating : SpeechEvent { override val readoutItemKey = ReadoutItemKey.VEHICLE_DAMAGE }
}
