package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kurou.kodriver.domain.repository.TextToSpeechRepository
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Androidの [TextToSpeech] でテキストを読み上げる [TextToSpeechRepository]。
 *
 * [TextToSpeech] の初期化は非同期（`OnInitListener`）で完了するため、最初の読み上げ要求時に
 * 初期化の完了を待ち合わせてからエンジンを使う。初期化に失敗した場合・読み上げ言語が
 * 利用できない場合は、以降の読み上げを行わず `isAvailable()` も `false` を返す。
 *
 * [speak] は [UtteranceProgressListener] で発話の完了（またはエラー）通知を待ち合わせるため、
 * 実際に読み上げが終わるまで（あるいは [stop] やコルーチンのキャンセルで打ち切られるまで）
 * suspendする。`:core:narrator` の `WavNarratorEngine` は「WAV再生と同じコルーチン上で
 * 完了・割り込みを扱える」ことを前提に `customSpeak` フックへこのRepositoryを渡しているため、
 * ここが即座に返ってしまうと発話中に次のイベントの音声が重なってしまう。
 *
 * @param textToSpeechFactory `OnInitListener` を受け取って [TextToSpeech] を生成する。
 *   `Context` への依存をKoinモジュール側に閉じ込め、テストではFakeを渡せるようにするためラムダで受ける。
 * @param locale 読み上げに使う言語。読み上げ文言は日本語のため既定は [Locale.JAPANESE]。
 */
internal class AndroidTextToSpeechRepository(
    private val textToSpeechFactory: (TextToSpeech.OnInitListener) -> TextToSpeech,
    private val locale: Locale = Locale.JAPANESE,
) : TextToSpeechRepository {
    private val mutex = Mutex()
    private var initialized = false
    private var textToSpeech: TextToSpeech? = null
    private val pendingUtterances = ConcurrentHashMap<String, CompletableDeferred<Unit>>()
    private val utteranceIdSequence = AtomicLong()

    override suspend fun isAvailable(): Boolean = ensureInitialized() != null

    override suspend fun speak(
        text: String,
        queue: Boolean,
    ) {
        if (text.isBlank()) return
        val engine = ensureInitialized() ?: return
        val utteranceId = "kodriver_tts_${utteranceIdSequence.incrementAndGet()}"
        val completed = CompletableDeferred<Unit>()
        pendingUtterances[utteranceId] = completed
        engine.speak(
            text,
            if (queue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId,
        )
        try {
            completed.await()
        } catch (e: CancellationException) {
            engine.stop()
            throw e
        } finally {
            pendingUtterances.remove(utteranceId)
        }
    }

    override suspend fun stop() {
        mutex.withLock { textToSpeech?.stop() }
    }

    /**
     * 初回呼び出し時のみ [TextToSpeech] を生成し、初期化完了を待つ。
     * 利用できない場合は `null` を返し、以降は再初期化しない。
     */
    private suspend fun ensureInitialized(): TextToSpeech? =
        mutex.withLock {
            if (initialized) return@withLock textToSpeech
            initialized = true
            val initStatus = CompletableDeferred<Int>()
            val engine = textToSpeechFactory { status -> initStatus.complete(status) }
            textToSpeech =
                if (initStatus.await() == TextToSpeech.SUCCESS &&
                    engine.setLanguage(locale) >= TextToSpeech.LANG_AVAILABLE
                ) {
                    engine.setOnUtteranceProgressListener(
                        object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit

                            override fun onDone(utteranceId: String?) = completeUtterance(utteranceId)

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) = completeUtterance(utteranceId)
                        },
                    )
                    engine
                } else {
                    engine.shutdown()
                    null
                }
            textToSpeech
        }

    /** [onDone] / [onError] のコールバックスレッドから呼ばれ、対応する [speak] 呼び出し側を再開させる。 */
    private fun completeUtterance(utteranceId: String?) {
        utteranceId?.let { pendingUtterances.remove(it)?.complete(Unit) }
    }
}
