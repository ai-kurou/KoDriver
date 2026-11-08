package kurou.kodriver.data.manualreadout

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository

/**
 * シミュレーターごとの再生状態をメモリに持ち、いずれかが再生中なら再生中とみなす。
 */
class InMemorySpeechPlaybackStateRepository : SpeechPlaybackStateRepository {
    private val activeSources = mutableSetOf<String>()
    private val _isActive = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    override fun update(
        sourceId: String,
        active: Boolean,
    ) {
        synchronized(activeSources) {
            if (active) activeSources.add(sourceId) else activeSources.remove(sourceId)
            _isActive.value = activeSources.isNotEmpty()
        }
    }
}
