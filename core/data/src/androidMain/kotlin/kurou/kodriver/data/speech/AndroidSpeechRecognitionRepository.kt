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
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

/** 認識結果の通知後、次の聞き取りを始めるまでの待ち時間。 */
internal const val SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS = 100L

/**
 * [SpeechRecognitionEngine] を使い、聞き取りセッションを繰り返して常時聞き取りを実現する [SpeechRecognitionRepository]。
 *
 * セッションは結果通知かエラーで終わるため、終わるたびに再開する。再開待ち時間とエラーごとの扱いは
 * [speechRecognitionErrorAction] に従う。権限がない・音声認識を利用できない場合は
 * [SpeechRecognitionEvent.Failed] を流して完了する。
 *
 * @param engineFactory セッションごとにエンジンを生成する。`Context` への依存をKoinモジュール側に閉じ込め、
 *   テストではFakeを渡せるようにするためラムダで受ける。
 * @param recognitionAvailabilityChecker 端末で音声認識を利用できるかを返す。
 * @param mainDispatcher `SpeechRecognizer` はメインスレッドでしか扱えないため、聞き取りはこの上で行う。
 */
internal class AndroidSpeechRecognitionRepository(
    private val engineFactory: () -> SpeechRecognitionEngine,
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
            while (true) {
                when (val end = listenOnce()) {
                    is SessionEnd.Restart -> {
                        delay(end.delayMs)
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
    private suspend fun FlowCollector<SpeechRecognitionEvent>.listenOnce(): SessionEnd {
        val signals = Channel<Signal>(Channel.UNLIMITED)
        val engine = engineFactory()
        try {
            try {
                engine.start(ChannelListener(signals))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return SessionEnd.Stop(SpeechRecognitionFailureReason.Unavailable)
            }
            while (true) {
                when (val signal = signals.receive()) {
                    Signal.Ready -> {
                        emit(SpeechRecognitionEvent.Listening)
                    }

                    is Signal.Partial -> {
                        emit(SpeechRecognitionEvent.Partial(signal.text))
                    }

                    is Signal.Results -> {
                        if (signal.candidates.isNotEmpty()) {
                            emit(SpeechRecognitionEvent.Recognized(signal.candidates))
                        }
                        return SessionEnd.Restart(SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS)
                    }

                    is Signal.Error -> {
                        return when (val action = speechRecognitionErrorAction(signal.errorCode)) {
                            is SpeechRecognitionErrorAction.Restart -> SessionEnd.Restart(action.delayMs)
                            is SpeechRecognitionErrorAction.Stop -> SessionEnd.Stop(action.reason)
                        }
                    }
                }
            }
        } finally {
            engine.stop()
            signals.close()
        }
    }

    private sealed interface Signal {
        data object Ready : Signal

        data class Partial(
            val text: String,
        ) : Signal

        data class Results(
            val candidates: List<String>,
        ) : Signal

        data class Error(
            val errorCode: Int,
        ) : Signal
    }

    private sealed interface SessionEnd {
        data class Restart(
            val delayMs: Long,
        ) : SessionEnd

        data class Stop(
            val reason: SpeechRecognitionFailureReason,
        ) : SessionEnd
    }

    /** エンジンのコールバックを [Signal] に変えて、フローを収集するコルーチンへ渡す。 */
    private class ChannelListener(
        private val signals: Channel<Signal>,
    ) : SpeechRecognitionEngineListener {
        override fun onReady() {
            signals.trySend(Signal.Ready)
        }

        override fun onPartial(text: String) {
            if (text.isNotBlank()) signals.trySend(Signal.Partial(text))
        }

        override fun onResults(candidates: List<String>) {
            signals.trySend(Signal.Results(candidates.filter { it.isNotBlank() }))
        }

        override fun onError(errorCode: Int) {
            signals.trySend(Signal.Error(errorCode))
        }
    }
}
