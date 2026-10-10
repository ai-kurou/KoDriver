package kurou.kodriver.domain.preview

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * 解決済みの自由文言を、項目の開始音の後に試聴する。
 * 利用可否は所有者の [scope] で一度取得し、購読の有無にかかわらず保持する。
 * [onPreviewClicked] は所有者のスコープで試聴を開始し、再押しで停止する。
 * [preview] は呼び出し元のコルーチンで実行し、キャンセルと例外をそのまま伝播する。
 */
class ReadoutTextPreviewHelper(
    private val scope: CoroutineScope,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
    private val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    private val speakText: SpeakTextUseCase,
) {
    private val guard = ReadoutPreviewGuard(scope, checkTextToSpeechAvailable, observeSoundVolume)
    val textToSpeechAvailable = guard.textToSpeechAvailable
    private val _isPreviewing = MutableStateFlow(false)
    val isPreviewing: StateFlow<Boolean> = _isPreviewing
    private var previewJob: Job? = null
    private var previewRequest = 0

    /** 再生中または開始待ちの試聴を止める。 */
    fun stop() {
        previewRequest++
        previewJob?.cancel()
        _isPreviewing.update { false }
    }

    /** 試聴中に呼ぶと停止し、停止中に呼ぶと開始音の後に [text] を読み上げる。 */
    fun onPreviewClicked(
        text: String,
        key: ReadoutItemKey,
    ) {
        val stopPreview = isPreviewing.value
        val request = ++previewRequest
        previewJob?.cancel()
        _isPreviewing.update { false }
        if (stopPreview) return
        previewJob =
            scope.launch {
                try {
                    val volume = guard.volumeForPreview(text) ?: return@launch
                    _isPreviewing.update { true }
                    playStartSoundForKey(key)
                    speakText(text, volume = volume)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // 試聴に失敗しても画面の操作を続けられるようにする。
                } finally {
                    if (previewRequest == request) _isPreviewing.update { false }
                }
            }
    }

    /** 空白文言・TTS利用不可・音量0以下では開始音も本文も再生しない。 */
    suspend fun preview(
        text: String,
        key: ReadoutItemKey,
    ) {
        val volume = guard.volumeForPreview(text) ?: return
        playStartSoundForKey(key)
        speakText(text, volume = volume)
    }
}
