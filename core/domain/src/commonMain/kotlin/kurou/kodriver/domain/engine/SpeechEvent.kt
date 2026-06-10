package kurou.kodriver.domain.engine

sealed interface SpeechEvent {
    data object CarLeft : SpeechEvent
    data object CarRight : SpeechEvent
    data object BlueFlag : SpeechEvent
    data object YellowFlag : SpeechEvent
    data object FullCourseYellow : SpeechEvent
    data object SessionStop : SpeechEvent
}
