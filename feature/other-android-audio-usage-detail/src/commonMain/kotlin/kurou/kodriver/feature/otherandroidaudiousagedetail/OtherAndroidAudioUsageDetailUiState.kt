package kurou.kodriver.feature.otherandroidaudiousagedetail

import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT
import kurou.kodriver.domain.model.AndroidAudioUsage

/** 音声の用途設定画面の表示状態。 */
data class OtherAndroidAudioUsageDetailUiState(
    val usage: AndroidAudioUsage = ANDROID_AUDIO_USAGE_DEFAULT,
    val previewingUsage: AndroidAudioUsage? = null,
)
