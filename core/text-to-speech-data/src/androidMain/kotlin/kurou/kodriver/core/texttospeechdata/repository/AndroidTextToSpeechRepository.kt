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
import java.util.concurrent.atomic.AtomicReference

/**
 * Androidの [TextToSpeech] でテキストを読み上げる [TextToSpeechRepository]。
 *
 * [TextToSpeech] の初期化は非同期（`OnInitListener`）で完了するため、最初の読み上げ要求時に
 * 初期化の完了を待ち合わせてからエンジンを使う。初期化に失敗した場合・読み上げ言語が
 * 利用できない場合・発話完了通知の登録に失敗した場合は、以降の読み上げを行わず
 * `isAvailable()` も `false` を返す。
 *
 * [speak] は [UtteranceProgressListener] で発話の完了（エラー・打ち切りを含む）通知を待ち合わせるため、
 * 実際に読み上げが終わるまで（あるいは [stop] やコルーチンのキャンセルで打ち切られるまで）
 * suspendする。`:core:narrator` の `WavNarratorEngine` は「WAV再生と同じコルーチン上で
 * 完了・割り込みを扱える」ことを前提に `customSpeak` フックへこのRepositoryを渡しているため、
 * ここが即座に返ってしまうと発話中に次のイベントの音声が重なってしまう。
 *
 * このRepositoryはKoinで `single` 登録されアプリ全体で共有されるため、コルーチンのキャンセルで
 * [TextToSpeech.stop] （エンジン全体の発話を止める）を呼ぶのは、キャンセルされた呼び出しが
 * まだ最新の発話要求である場合に限る。既に別の呼び出しに [TextToSpeech.QUEUE_FLUSH] 等で
 * 上書きされた古い呼び出しがキャンセルされても、無関係な新しい発話を巻き添えで止めない。
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
    private val activeUtteranceId = AtomicReference<String?>()

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
        activeUtteranceId.set(utteranceId)
        val result =
            engine.speak(
                text,
                if (queue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId,
            )
        if (result != TextToSpeech.SUCCESS) {
            // 発話要求自体が失敗した場合、onDone/onError/onStopのいずれも呼ばれないため
            // completed.await()がハングしてしまう。要求前に諦めて即座に返す。
            pendingUtterances.remove(utteranceId)
            return
        }
        try {
            completed.await()
        } catch (e: CancellationException) {
            // 既に別の発話要求（QUEUE_FLUSH等）へ上書きされている場合、このキャンセルで
            // engine.stop()を呼ぶと無関係な新しい発話まで止めてしまうため、まだこの呼び出しが
            // 最新（＝実際にエンジン上でアクティブ）な場合のみ停止する。
            if (activeUtteranceId.get() == utteranceId) {
                engine.stop()
            }
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
                    engine.setLanguage(locale) >= TextToSpeech.LANG_AVAILABLE &&
                    engine.setOnUtteranceProgressListener(
                        object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) = Unit

                            override fun onDone(utteranceId: String?) = completeUtterance(utteranceId)

                            override fun onStop(
                                utteranceId: String?,
                                interrupted: Boolean,
                            ) = completeUtterance(utteranceId)

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) = completeUtterance(utteranceId)
                        },
                    ) == TextToSpeech.SUCCESS
                ) {
                    engine
                } else {
                    engine.shutdown()
                    null
                }
            textToSpeech
        }

    /**
     * [onDone] / [onError] / [onStop][UtteranceProgressListener.onStop] のコールバックスレッドから呼ばれ、
     * 対応する [speak] 呼び出し側を再開させる。`stop()` や新しい発話要求（`QUEUE_FLUSH`）で
     * 打ち切られた場合は `onDone`/`onError` ではなく `onStop` が呼ばれるため、これも完了扱いにしないと
     * 打ち切られた側の `speak()` が再開しないまま残ってしまう。
     */
    private fun completeUtterance(utteranceId: String?) {
        utteranceId?.let { pendingUtterances.remove(it)?.complete(Unit) }
    }
}
