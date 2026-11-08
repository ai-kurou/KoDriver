package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.model.SpeechRecognitionChime
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.SpeechPlaybackStateRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

/** [SpeechRecognitionRepository.setMode] で渡されたモードを記録するフェイク。認識イベントは流さない。 */
internal class FakeSpeechRecognitionModeRepository : SpeechRecognitionRepository {
    val modes = mutableListOf<SpeechRecognitionMode>()

    override fun isAvailable(): Boolean = true

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> = emptyFlow()

    override fun setMode(mode: SpeechRecognitionMode) {
        modes += mode
    }
}

/** 読み上げの再生状態をテストから切り替えられるフェイク。 */
internal class FakeSpeechPlaybackStateRepository : SpeechPlaybackStateRepository {
    private val active = MutableStateFlow(false)
    override val isActive: StateFlow<Boolean> = active

    override fun update(
        sourceId: String,
        active: Boolean,
    ) {
        this.active.value = active
    }
}

/** 鳴らした確認音を記録するフェイク。[failure] を設定すると再生時にその例外を投げる。 */
internal class FakeSpeechRecognitionChimePlayer : SpeechRecognitionChimePlayer {
    val played = mutableListOf<SpeechRecognitionChime>()
    val playCount: Int get() = played.size
    var failure: Exception? = null

    /** true の間は再生が終わらず、取り消されたときに [cancelledChimes] へ記録する。 */
    var holdPlayback = false
    val cancelledChimes = mutableListOf<SpeechRecognitionChime>()
    val cancelled: Boolean get() = cancelledChimes.isNotEmpty()

    override suspend fun play(chime: SpeechRecognitionChime) {
        played += chime
        failure?.let { throw it }
        if (holdPlayback) {
            try {
                awaitCancellation()
            } catch (e: CancellationException) {
                cancelledChimes += chime
                throw e
            }
        }
    }
}
