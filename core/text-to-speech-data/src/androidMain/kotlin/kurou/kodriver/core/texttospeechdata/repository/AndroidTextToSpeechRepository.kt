package kurou.kodriver.core.texttospeechdata.repository

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
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
 * 実際にエンジン上で再生中（[UtteranceProgressListener.onStart] 済み）である場合に限る。
 * まだ再生開始前（＝他の発話の後ろにキューイングされているだけ）の呼び出しがキャンセルされても、
 * 現在再生中の無関係な発話を巻き添えで止めない。
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
    private var unavailableReason: TextToSpeechUnavailableReason? = null
    private val pendingUtterances = ConcurrentHashMap<String, CompletableDeferred<Unit>>()
    private val utteranceIdSequence = AtomicLong()
    private val activeUtteranceId = AtomicReference<String?>()

    override suspend fun isAvailable(): Boolean = ensureInitialized() != null

    /**
     * 利用できない理由を返す。[ensureInitialized] が設定する [unavailableReason] を返すため、
     * 未初期化なら先に初期化を待ち合わせる。
     *
     * 前回の初期化が失敗している場合は、ユーザーがエンジンや日本語データを導入した可能性があるため
     * 再初期化を試みる。読み上げ（[speak]）のたびにエンジンを生成し直さないよう、再試行はここでのみ行う。
     */
    override suspend fun unavailableReason(): TextToSpeechUnavailableReason? {
        ensureInitialized(retryIfUnavailable = true)
        return unavailableReason
    }

    override suspend fun speak(
        text: String,
        queue: Boolean,
    ) {
        if (text.isBlank()) return
        val engine = ensureInitialized() ?: return
        val utteranceId = "kodriver_tts_${utteranceIdSequence.incrementAndGet()}"
        val completed = CompletableDeferred<Unit>()
        pendingUtterances[utteranceId] = completed
        if (!queue) {
            // QUEUE_FLUSHは現在再生中・キュー中の発話をすべて打ち切るため、それらに対応する
            // speak()呼び出しがonStop通知を待ち続けないよう、ここで明示的に完了させる。
            completePendingUtterancesExcept(utteranceId)
        }
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
            // まだ再生開始前（他の発話の後ろにキューイングされているだけ）の呼び出しがキャンセルされても、
            // engine.stop()を呼ぶと現在再生中の無関係な発話まで止めてしまうため、このutteranceIdが
            // 実際に再生中（onStart済み）の場合のみ停止する。
            if (activeUtteranceId.get() == utteranceId) {
                engine.stop()
            }
            throw e
        } finally {
            pendingUtterances.remove(utteranceId)
        }
    }

    override suspend fun stop() {
        mutex.withLock {
            textToSpeech?.stop()
            // stop()はonStopを発火させるが、コールバックが来ない経路（未再生のキュー分等）に
            // 備えて、残っているpendingUtterancesもここで明示的に完了させる。
            completePendingUtterancesExcept(exceptUtteranceId = null)
        }
    }

    /**
     * 初回呼び出し時のみ [TextToSpeech] を生成し、初期化完了を待つ。
     * 利用できない場合は `null` を返し、[unavailableReason] にその理由を記録する。以降の [speak] / [isAvailable] では再初期化せず、
     * [unavailableReason] の呼び出し時のみ再初期化する。
     *
     * 理由の切り分けは、[TextToSpeech.OnInitListener] の結果と [TextToSpeech.setLanguage] の結果の
     * どちらで失敗したかで行う。エンジンサービス自体が端末に存在しない・バインドに失敗した場合は
     * 初期化そのものが `SUCCESS` 以外で完了するため [TextToSpeechUnavailableReason.EngineMissing] とし、
     * 初期化自体は成功したが言語データ（日本語）が無い場合は [TextToSpeechUnavailableReason.LanguageDataMissing] とする。
     * 発話完了通知の登録失敗はエンジン自体の異常とみなし [TextToSpeechUnavailableReason.EngineMissing] 扱いにする。
     */
    private suspend fun ensureInitialized(retryIfUnavailable: Boolean = false): TextToSpeech? =
        mutex.withLock {
            if (initialized && !(retryIfUnavailable && textToSpeech == null)) return@withLock textToSpeech
            initialized = true
            unavailableReason = null
            val initStatus = CompletableDeferred<Int>()
            val engine = textToSpeechFactory { status -> initStatus.complete(status) }
            if (initStatus.await() != TextToSpeech.SUCCESS) {
                unavailableReason = TextToSpeechUnavailableReason.EngineMissing
                engine.shutdown()
                textToSpeech = null
                return@withLock null
            }
            if (engine.setLanguage(locale) < TextToSpeech.LANG_AVAILABLE) {
                unavailableReason = TextToSpeechUnavailableReason.LanguageDataMissing
                engine.shutdown()
                textToSpeech = null
                return@withLock null
            }
            val listenerRegistered =
                engine.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            activeUtteranceId.set(utteranceId)
                        }

                        override fun onDone(utteranceId: String?) = completeUtterance(utteranceId)

                        override fun onStop(
                            utteranceId: String?,
                            interrupted: Boolean,
                        ) = completeUtterance(utteranceId)

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) = completeUtterance(utteranceId)
                    },
                ) == TextToSpeech.SUCCESS
            textToSpeech =
                if (listenerRegistered) {
                    engine
                } else {
                    unavailableReason = TextToSpeechUnavailableReason.EngineMissing
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

    /**
     * [exceptUtteranceId] 以外の残っているpendingな発話をすべて即座に完了させる。
     * `QUEUE_FLUSH`（新しい発話要求）や明示的な [stop] は、Androidが実際に `onStop` を
     * 通知するかどうかに関わらず対象の発話をすべて打ち切るため、コールバックを待たずここで解決する。
     */
    private fun completePendingUtterancesExcept(exceptUtteranceId: String?) {
        pendingUtterances.keys
            .filter { it != exceptUtteranceId }
            .forEach { id -> pendingUtterances.remove(id)?.complete(Unit) }
    }
}
