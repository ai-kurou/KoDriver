package kurou.kodriver.domain.model

public enum class AndroidAudioUsage(
    public val id: String,
) {
    MEDIA("media"),
    COMMUNICATION("communication"),
    RINGTONE("ringtone"),
    NOTIFICATION("notification"),
    ALARM("alarm"),
    ASSISTANT("assistant"),
    ;

    public companion object {
        public fun fromId(id: String): AndroidAudioUsage =
            entries.firstOrNull { it.id == id } ?: ANDROID_AUDIO_USAGE_DEFAULT
    }
}
