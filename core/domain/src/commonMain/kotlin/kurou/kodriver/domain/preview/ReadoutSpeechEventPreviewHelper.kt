package kurou.kodriver.domain.preview

import kotlinx.coroutines.CoroutineScope
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.StopSpeechUseCase

/**
 * 解決済みの自由文言を持つイベントを、既存のイベント再生経路で試聴する。
 * 利用可否は所有者の [scope] で一度取得し、初期値falseで保持する。
 * 再生条件の確認は呼び出し元のコルーチンで実行し、キャンセルと例外をそのまま伝播する。
 */
class ReadoutSpeechEventPreviewHelper(
    scope: CoroutineScope,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
    private val stopSpeech: StopSpeechUseCase,
) {
    private val guard = ReadoutPreviewGuard(scope, checkTextToSpeechAvailable, observeSoundVolume)
    val textToSpeechAvailable = guard.textToSpeechAvailable

    private var startedKey: ReadoutItemKey? = null
    private var stopGeneration = 0

    /**
     * このヘルパーで開始した試聴を止める。開始待ちの試聴は無効化する。
     * 試聴が自然終了している場合や別項目の読み上げ中は、本番の読み上げを止めない。
     */
    fun stop() {
        stopGeneration++
        val key = startedKey ?: return
        startedKey = null
        stopSpeech(key)
    }

    /** 空白文言・TTS利用不可・音量0以下ではイベントを再生しない。 */
    suspend fun preview(
        text: String,
        event: SpeechEvent,
    ) {
        val generation = stopGeneration
        guard.volumeForPreview(text) ?: return
        if (generation != stopGeneration) return
        playSpeechEvent(event)
        startedKey = event.readoutItemKey
    }
}
