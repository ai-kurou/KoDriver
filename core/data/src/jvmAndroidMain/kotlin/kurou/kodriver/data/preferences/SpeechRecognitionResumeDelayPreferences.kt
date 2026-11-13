package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class SpeechRecognitionResumeDelayPreferences(
    @ProtoNumber(1) val resumeDelayMs: Int = SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT,
)
