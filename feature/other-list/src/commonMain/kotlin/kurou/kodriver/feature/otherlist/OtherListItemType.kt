package kurou.kodriver.feature.otherlist

/**
 * OtherList の項目種別。
 */
sealed interface OtherListItemType {
    val id: String

    /** [entries] 内の宣言順。一覧の並び替えに使う。 */
    val ordinal: Int get() = entries.indexOf(this)

    sealed interface ReadoutSettings : OtherListItemType

    sealed interface OverlaySettings : OtherListItemType

    sealed interface AppSettings : OtherListItemType

    data object AccessLocalNetworkPermission : OtherListItemType {
        override val id: String = "access_local_network_permission"
    }

    data object ServerIp : OtherListItemType {
        override val id: String = "server_ip"
    }

    data object ConsoleIp : OtherListItemType {
        override val id: String = "console_ip"
    }

    data object Volume : ReadoutSettings {
        override val id: String = "volume"
    }

    data object TtsEngineMissing : ReadoutSettings {
        override val id: String = "tts_engine_missing"
    }

    data object TtsLanguageDataMissing : ReadoutSettings {
        override val id: String = "tts_language_data_missing"
    }

    data object WindowsSpeechUnavailable : ReadoutSettings {
        override val id: String = "windows_speech_unavailable"
    }

    data object KeepScreenOn : AppSettings {
        override val id: String = "keep_screen_on"
    }

    data object ReadoutStartSound : ReadoutSettings {
        override val id: String = "readout_start_sound"
    }

    data object Voice : ReadoutSettings {
        override val id: String = "voice"
    }

    data object VoiceSpeed : ReadoutSettings {
        override val id: String = "voice_speed"
    }

    data object VoicePitch : ReadoutSettings {
        override val id: String = "voice_pitch"
    }

    data object AndroidAudioUsage : ReadoutSettings {
        override val id: String = "android_audio_usage"
    }

    data object Theme : AppSettings {
        override val id: String = "theme"
    }

    data object OverlayVisible : OverlaySettings {
        override val id: String = "overlay_visible"
    }

    data object OverlayTextSize : OverlaySettings {
        override val id: String = "overlay_text_size"
    }

    data object OverlayTextColor : OverlaySettings {
        override val id: String = "overlay_text_color"
    }

    data object OverlayBackgroundOpacity : OverlaySettings {
        override val id: String = "overlay_background_opacity"
    }

    data object DynamicColor : AppSettings {
        override val id: String = "dynamic_color"
    }

    data object HapticFeedback : AppSettings {
        override val id: String = "haptic_feedback"
    }

    data object Startup : AppSettings {
        override val id: String = "startup"
    }

    data object DeleteAllLogs : OtherListItemType {
        override val id: String = "delete_all_logs"
    }

    data object GitHubRepository : OtherListItemType {
        override val id: String = "github_repository"
    }

    data object ReleasePage : OtherListItemType {
        override val id: String = "release_page"
    }

    data object Feedback : OtherListItemType {
        override val id: String = "feedback"
    }

    data object License : OtherListItemType {
        override val id: String = "license"
    }

    data object DebugState : OtherListItemType {
        override val id: String = "debug_state"
    }

    companion object {
        /** 永続化・ナビゲーション互換のため、既存の宣言順を維持する。 */
        val entries: List<OtherListItemType> by lazy {
            listOf(
                AccessLocalNetworkPermission,
                ServerIp,
                ConsoleIp,
                Volume,
                TtsEngineMissing,
                TtsLanguageDataMissing,
                WindowsSpeechUnavailable,
                KeepScreenOn,
                ReadoutStartSound,
                Voice,
                VoiceSpeed,
                VoicePitch,
                AndroidAudioUsage,
                Theme,
                OverlayVisible,
                OverlayTextSize,
                OverlayTextColor,
                OverlayBackgroundOpacity,
                DynamicColor,
                HapticFeedback,
                Startup,
                DeleteAllLogs,
                GitHubRepository,
                ReleasePage,
                Feedback,
                License,
                DebugState,
            )
        }

        fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
