package kurou.kodriver.feature.otherandroidaudiousagedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.AndroidAudioUsage

/** 保存済み用途と試聴中用途から画面状態を合成する冷たい Flow。 */
internal class OtherAndroidAudioUsageDetailFlow(
    usage: Flow<AndroidAudioUsage>,
    previewingUsage: Flow<AndroidAudioUsage?>,
) {
    val uiState: Flow<OtherAndroidAudioUsageDetailUiState> =
        combine(usage, previewingUsage) { usage, preview ->
            OtherAndroidAudioUsageDetailUiState(usage = usage, previewingUsage = preview)
        }
}
