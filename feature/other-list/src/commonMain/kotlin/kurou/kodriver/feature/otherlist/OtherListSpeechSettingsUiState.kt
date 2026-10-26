package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.SOUND_VOLUME_DEFAULT
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT

data class OtherListSpeechSettingsUiState(
    val voiceId: String = VOICE_ID_UNSPECIFIED,
    val voiceSpeed: Float = VOICE_SPEED_DEFAULT,
    val voicePitch: Float = VOICE_PITCH_DEFAULT,
    val androidAudioUsage: AndroidAudioUsage = ANDROID_AUDIO_USAGE_DEFAULT,
    val readoutStartSoundType: ReadoutStartSoundType = READOUT_START_SOUND_TYPE_DEFAULT,
    val soundVolume: Int = SOUND_VOLUME_DEFAULT,
)
