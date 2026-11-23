package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

/**
 * シミュレーターごとの再生状態をメモリに持ち、いずれかが再生中なら再生中とみなす。
 */
internal class InMemorySpeechPlaybackStateRepository : SpeechPlaybackStateRepository {
    private val activeSources = mutableSetOf<String>()
    override val isActive: StateFlow<Boolean>
        field = MutableStateFlow(false)

    override fun update(
        sourceId: String,
        active: Boolean,
    ) {
        synchronized(activeSources) {
            if (active) activeSources.add(sourceId) else activeSources.remove(sourceId)
            isActive.value = activeSources.isNotEmpty()
        }
    }
}
