package kurou.kodriver.domain.model

/** インストール済みの読み上げ音声。 [id] はSAPIのVoiceInfo.Nameで、SelectVoiceに渡す値。 */
data class TextToSpeechVoice(
    val id: String,
    val displayName: String,
    val cultureName: String,
    /** システム既定として使われる音声か。 */
    val isDefault: Boolean = false,
)
