package kurou.kodriver.domain.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase

/** 自由文言とイベントの試聴に共通する利用可否と再生条件を管理する。 */
internal class ReadoutPreviewGuard(
    scope: CoroutineScope,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) {
    val textToSpeechAvailable =
        flow { emit(checkTextToSpeechAvailable()) }
            .stateIn(scope, SharingStarted.Eagerly, false)

    /** 再生可能な場合のみ現在の音量を返し、空白・利用不可では音量を取得しない。 */
    suspend fun volumeForPreview(text: String): Int? {
        if (text.isBlank() || !textToSpeechAvailable.value) return null
        val volume = observeSoundVolume().first()
        return volume.takeIf { it > 0 }
    }
}
