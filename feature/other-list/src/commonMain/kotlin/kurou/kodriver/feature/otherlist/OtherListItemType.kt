package kurou.kodriver.feature.otherlist

/**
 * OtherList の項目種別。
 */
public sealed interface OtherListItemType {
    public val id: String

    /** [entries] 内の宣言順。一覧の並び替えに使う。 */
    public val ordinal: Int get() = entries.indexOf(this)

    public sealed interface ReadoutSettings : OtherListItemType

    public sealed interface OverlaySettings : OtherListItemType

    public sealed interface MicrophoneSettings : OtherListItemType

    public sealed interface AppSettings : OtherListItemType

    public data object AccessLocalNetworkPermission : OtherListItemType {
        override val id: String = "access_local_network_permission"
    }

    public data object ServerIp : OtherListItemType {
        override val id: String = "server_ip"
    }

    public data object ConsoleIp : OtherListItemType {
        override val id: String = "console_ip"
    }

    public data object Volume : ReadoutSettings {
        override val id: String = "volume"
    }

    public data object TtsEngineMissing : ReadoutSettings {
        override val id: String = "tts_engine_missing"
    }

    public data object TtsLanguageDataMissing : ReadoutSettings {
        override val id: String = "tts_language_data_missing"
    }

    public data object WindowsSpeechUnavailable : ReadoutSettings {
        override val id: String = "windows_speech_unavailable"
    }

    public data object KeepScreenOn : AppSettings {
        override val id: String = "keep_screen_on"
    }

    public data object ReadoutStartSound : ReadoutSettings {
        override val id: String = "readout_start_sound"
    }

    public data object Voice : ReadoutSettings {
        override val id: String = "voice"
    }

    public data object VoiceSpeed : ReadoutSettings {
        override val id: String = "voice_speed"
    }

    public data object VoicePitch : ReadoutSettings {
        override val id: String = "voice_pitch"
    }

    public data object AndroidAudioUsage : ReadoutSettings {
        override val id: String = "android_audio_usage"
    }

    public data object Theme : AppSettings {
        override val id: String = "theme"
    }

    public data object OverlayVisible : OverlaySettings {
        override val id: String = "overlay_visible"
    }

    public data object OverlayTextSize : OverlaySettings {
        override val id: String = "overlay_text_size"
    }

    public data object OverlayTextColor : OverlaySettings {
        override val id: String = "overlay_text_color"
    }

    public data object OverlayBackgroundOpacity : OverlaySettings {
        override val id: String = "overlay_background_opacity"
    }

    public data object MicrophoneCutQuietSound : MicrophoneSettings {
        override val id: String = "microphone_cut_quiet_sound"
    }

    public data object DynamicColor : AppSettings {
        override val id: String = "dynamic_color"
    }

    public data object HapticFeedback : AppSettings {
        override val id: String = "haptic_feedback"
    }

    public data object Startup : AppSettings {
        override val id: String = "startup"
    }

    public data object ShowLogs : OtherListItemType {
        override val id: String = "show_logs"
    }

    public data object ResetAutoReadoutSettings : OtherListItemType {
        override val id: String = "reset_auto_readout_settings"
    }

    public data object ResetManualReadoutSettings : OtherListItemType {
        override val id: String = "reset_manual_readout_settings"
    }

    public data object DeleteAllLogs : OtherListItemType {
        override val id: String = "delete_all_logs"
    }

    public data object GitHubRepository : OtherListItemType {
        override val id: String = "github_repository"
    }

    public data object ReleasePage : OtherListItemType {
        override val id: String = "release_page"
    }

    public data object Feedback : OtherListItemType {
        override val id: String = "feedback"
    }

    public data object License : OtherListItemType {
        override val id: String = "license"
    }

    public data object DebugState : OtherListItemType {
        override val id: String = "debug_state"
    }

    public companion object {
        /** 一覧の表示順を決める宣言順。永続化されるのは [id] であり、順序ではない。 */
        public val entries: List<OtherListItemType> by lazy {
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
                MicrophoneCutQuietSound,
                DynamicColor,
                HapticFeedback,
                Startup,
                GitHubRepository,
                ReleasePage,
                Feedback,
                License,
                DebugState,
                ShowLogs,
                ResetAutoReadoutSettings,
                ResetManualReadoutSettings,
                DeleteAllLogs,
            )
        }

        public fun fromId(id: String): OtherListItemType? = entries.firstOrNull { it.id == id }
    }
}
