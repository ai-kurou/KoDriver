package kurou.kodriver.data.speech

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import java.util.concurrent.atomic.AtomicReference

/** 認識結果の通知後、次の聞き取りを始めるまでの待ち時間。 */
internal const val SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS = 100L

/**
 * 認識サービスからコールバックが1つも来ないままセッションが止まった場合に、セッションを打ち切るまでの時間。
 * プラットフォームの無音タイムアウトより十分長くしてある。
 */
internal const val SPEECH_RECOGNITION_CALLBACK_TIMEOUT_MS = 30_000L

/**
 * [SpeechRecognitionEngine] を使い、聞き取りセッションを繰り返して常時聞き取りを実現する [SpeechRecognitionRepository]。
 *
 * セッションは結果通知かエラーで終わるため、終わるたびに再開する。再開待ち時間とエラーごとの扱いは
 * [speechRecognitionErrorAction] に従う。権限がない・音声認識を利用できない場合は
 * [SpeechRecognitionEvent.Failed] を流して完了する。端末内認識が言語エラーになった場合（日本語モデルが
 * 未導入など）は、通常の認識サービスに切り替えて再開し、それも失敗したときに利用不可として完了する。
 * 一定時間コールバックが来ないセッションは打ち切って再開する。
 *
 * @param engineFactory セッションごとにエンジンを生成する。引数は端末内認識を優先するか。
 *   `Context` への依存をKoinモジュール側に閉じ込め、テストではFakeを渡せるようにするためラムダで受ける。
 * @param recognitionAvailabilityChecker 端末で音声認識を利用できるかを返す。
 * @param mainDispatcher `SpeechRecognizer` はメインスレッドでしか扱えないため、聞き取りはこの上で行う。
 */
internal class AndroidSpeechRecognitionRepository(
    private val engineFactory: (preferOnDevice: Boolean) -> SpeechRecognitionEngine,
    private val recognitionAvailabilityChecker: () -> Boolean,
    private val permissionRepository: RecordAudioPermissionRepository,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : SpeechRecognitionRepository {
    override fun isAvailable(): Boolean = recognitionAvailabilityChecker()

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> =
        flow {
            val failure = startFailureReason()
            if (failure != null) {
                emit(SpeechRecognitionEvent.Failed(failure))
                return@flow
            }
            var preferOnDevice = true
            while (true) {
                when (val end = listenOnce(preferOnDevice)) {
                    is SessionEnd.Restart -> {
                        delay(end.delayMs)
                    }

                    SessionEnd.FallbackToStandard -> {
                        preferOnDevice = false
                    }

                    is SessionEnd.Stop -> {
                        emit(SpeechRecognitionEvent.Failed(end.reason))
                        return@flow
                    }
                }
            }
        }.flowOn(mainDispatcher)

    private fun startFailureReason(): SpeechRecognitionFailureReason? =
        when {
            !permissionRepository.isGranted() -> SpeechRecognitionFailureReason.PermissionDenied
            !recognitionAvailabilityChecker() -> SpeechRecognitionFailureReason.Unavailable
            else -> null
        }

    /** 1セッション分を聞き取り、イベントを流して、再開か停止かを返す。終了時は必ずエンジンを解放する。 */
    private suspend fun FlowCollector<SpeechRecognitionEvent>.listenOnce(preferOnDevice: Boolean): SessionEnd {
        val signals = Channel<Signal>(Channel.UNLIMITED)
        val listener = ChannelListener(signals)
        val engine = engineFactory(preferOnDevice)
        try {
            try {
                engine.start(listener)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return SessionEnd.Stop(SpeechRecognitionFailureReason.Unavailable)
            }
            while (true) {
                val signal =
                    withTimeoutOrNull(SPEECH_RECOGNITION_CALLBACK_TIMEOUT_MS) { signals.receive() }
                        ?: return SessionEnd.Restart(SPEECH_RECOGNITION_RETRY_DELAY_MS)
                when (signal) {
                    Signal.Ready -> {
                        emit(SpeechRecognitionEvent.Listening)
                    }

                    Signal.PartialAvailable -> {
                        listener.takePartial()?.let { emit(SpeechRecognitionEvent.Partial(it)) }
                    }

                    is Signal.Results -> {
                        if (signal.candidates.isNotEmpty()) {
                            emit(SpeechRecognitionEvent.Recognized(signal.candidates))
                        }
                        return SessionEnd.Restart(SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS)
                    }

                    is Signal.Error -> {
                        return sessionEndForError(signal.errorCode, engine.isOnDevice)
                    }
                }
            }
        } finally {
            engine.stop()
            signals.close()
        }
    }

    /**
     * 端末内認識の言語エラー（日本語モデルが未導入など）は通常の認識サービスなら扱える場合があるため、
     * 通常の認識へ切り替えて再開する。
     */
    private fun sessionEndForError(
        errorCode: Int,
        isOnDevice: Boolean,
    ): SessionEnd =
        when (val action = speechRecognitionErrorAction(errorCode)) {
            is SpeechRecognitionErrorAction.Restart -> {
                SessionEnd.Restart(action.delayMs)
            }

            is SpeechRecognitionErrorAction.Stop -> {
                if (isOnDevice && action.reason == SpeechRecognitionFailureReason.Unavailable) {
                    SessionEnd.FallbackToStandard
                } else {
                    SessionEnd.Stop(action.reason)
                }
            }
        }

    private sealed interface Signal {
        data object Ready : Signal

        /** 未配信の認識途中の文字列がある。取り出しは [ChannelListener.takePartial]。 */
        data object PartialAvailable : Signal

        data class Results(
            val candidates: List<String>,
        ) : Signal

        data class Error(
            val errorCode: Int,
        ) : Signal
    }

    private sealed interface SessionEnd {
        /** 端末内認識を諦め、通常の認識サービスで直ちに再開する。 */
        data object FallbackToStandard : SessionEnd

        data class Restart(
            val delayMs: Long,
        ) : SessionEnd

        data class Stop(
            val reason: SpeechRecognitionFailureReason,
        ) : SessionEnd
    }

    /**
     * エンジンのコールバックを [Signal] に変えて、フローを収集するコルーチンへ渡す。
     * 認識途中の文字列は最新の1件だけを保持し、収集側が遅くてもキューが伸びないようにする。
     * 確定結果とエラーは取りこぼさない。
     */
    private class ChannelListener(
        private val signals: Channel<Signal>,
    ) : SpeechRecognitionEngineListener {
        private val latestPartial = AtomicReference<String?>(null)

        fun takePartial(): String? = latestPartial.getAndSet(null)

        override fun onReady() {
            signals.trySend(Signal.Ready)
        }

        override fun onPartial(text: String) {
            if (text.isNotBlank() && latestPartial.getAndSet(text) == null) {
                signals.trySend(Signal.PartialAvailable)
            }
        }

        override fun onResults(candidates: List<String>) {
            signals.trySend(Signal.Results(candidates.filter { it.isNotBlank() }))
        }

        override fun onError(errorCode: Int) {
            signals.trySend(Signal.Error(errorCode))
        }
    }
}
