package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
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

/** 確認音の再生回数を数えるフェイク。[failure] を設定すると再生時にその例外を投げる。 */
internal class FakeSpeechRecognitionChimePlayer : SpeechRecognitionChimePlayer {
    var playCount = 0
    var failure: Exception? = null

    /** true の間は再生が終わらず、取り消されたときに [cancelled] を立てる。 */
    var holdPlayback = false
    var cancelled = false

    override suspend fun play() {
        playCount++
        failure?.let { throw it }
        if (holdPlayback) {
            try {
                awaitCancellation()
            } catch (e: CancellationException) {
                cancelled = true
                throw e
            }
        }
    }
}
