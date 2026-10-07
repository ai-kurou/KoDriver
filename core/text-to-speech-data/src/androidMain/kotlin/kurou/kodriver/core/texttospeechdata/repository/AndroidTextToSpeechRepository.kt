package kurou.kodriver.core.texttospeechdata.repository

import android.os.Bundle
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
 * @param volumeParamsFactory 音量（0.0〜1.0）を [TextToSpeech.speak] のparamsへ変換する。
 *   `Bundle` はJVMホストテストでは動作しないため、テストではFakeを渡せるようにしている。
 */
internal class AndroidTextToSpeechRepository(
    private val textToSpeechFactory: (TextToSpeech.OnInitListener) -> TextToSpeech,
    private val locale: Locale = Locale.JAPANESE,
    private val volumeParamsFactory: (Float) -> Bundle = { volume ->
        Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume) }
    },
) : TextToSpeechRepository {
    private val mutex = Mutex()
    private val voiceLock = Any()
    private var appliedVoiceId: String? = null
    private var defaultVoiceIdValue: String? = null

    /** 日本語の初期化で選ばれた音声ID。試聴や個別の音声指定では変えない。 */
    internal val defaultVoiceId: String?
        get() = synchronized(voiceLock) { defaultVoiceIdValue }

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

    /**
     * 一覧取得と読み上げで同じエンジンを共有する。初期化に失敗した場合は `null` を返す。
     *
     * [retryIfUnavailable] が `true` の場合、前回の初期化が失敗していれば再初期化を試みる。
     * 音声一覧の再読み込みで、後から導入した日本語データを反映するために使う。
     */
    internal suspend fun engineOrNull(retryIfUnavailable: Boolean = false): TextToSpeech? =
        ensureInitialized(retryIfUnavailable = retryIfUnavailable)

    /** [voiceId] に対応する音声を使い、未指定・見つからない場合は既定の日本語音声を使う。 */
    override suspend fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
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
            synchronized(voiceLock) {
                applyVoice(engine, voiceId)
                engine.speak(
                    text,
                    if (queue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH,
                    volumeParamsFactory(volume.coerceIn(0, 100) / VOLUME_SCALE),
                    utteranceId,
                )
            }
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

    /**
     * setVoiceはエンジン全体に残る設定のため最後に適用したIDを覚えて変更時のみ呼ぶ。
     * QUEUE_ADDで待機中の発話にも新しい声が適用される可能性があるが、設定変更は稀なので許容する。
     * 適用値とエンジンへの設定を排他し、発話完了を待つsuspendポイントではロックを保持しない。
     */
    private fun applyVoice(
        engine: TextToSpeech,
        voiceId: String,
    ) {
        if (appliedVoiceId == voiceId) return
        if (voiceId.isNotEmpty()) {
            val voice =
                try {
                    engine.voices?.firstOrNull { it.name == voiceId }
                } catch (_: Exception) {
                    null
                }
            if (voice == null || engine.setVoice(voice) != TextToSpeech.SUCCESS) {
                // 後から音声が導入された場合に適用できるよう、フォールバックした要求は適用済みとして覚えない。
                appliedVoiceId = null
                engine.setLanguage(locale)
                return
            }
        } else if (appliedVoiceId != null) {
            engine.setLanguage(locale)
        }
        appliedVoiceId = voiceId
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
            // 再初期化したエンジンには前の声が引き継がれないため、適用済みのIDを破棄する。
            synchronized(voiceLock) {
                appliedVoiceId = null
                defaultVoiceIdValue = null
            }
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
            synchronized(voiceLock) {
                defaultVoiceIdValue =
                    try {
                        engine.voice?.name
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
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

    private companion object {
        const val VOLUME_SCALE = 100f
    }
}
