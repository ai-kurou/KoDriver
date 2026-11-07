package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.captureErrorTypeOnly
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

/** 1回に読み取る音声の長さ（16kHz・モノラル・16bit PCM で100ミリ秒分のバイト数）。 */
internal const val VOSK_READ_CHUNK_BYTES = 3_200

/** マイクの取得・読み取りに連続して失敗したとき、利用不可として完了するまでの回数。 */
internal const val VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES = 10

/** この回数（100ミリ秒ずつで5秒分）続けて読み取れたら、マイクが正常に動いているとみなして連続失敗を数え直す。 */
internal const val VOSK_HEALTHY_READ_COUNT = 50

/** マイクの失敗後に再開するまでの待ち時間の初期値。失敗が続くたびに倍にする。 */
internal const val VOSK_RETRY_INITIAL_DELAY_MILLIS = 1_000L

/** マイクの失敗後に再開するまでの待ち時間の上限。 */
internal const val VOSK_RETRY_MAX_DELAY_MILLIS = 30_000L

/**
 * マイクを [PcmRecorder] で開きっぱなしにして [SpeechTranscriber]（Vosk）で認識する、Android の [SpeechRecognitionRepository]。
 *
 * OSの `SpeechRecognizer` は認識を始めるたびに開始音を鳴らすが、こちらは認識サービスを介さないため鳴らない。
 * 認識モデルは全ビルドに同梱するが、読み込めなかったときは利用不可になる。
 * マイクを開けない・読み取れない一時的な失敗（他アプリとの競合や入力の瞬断など）は、1秒から倍々に最大30秒まで待って再開し、
 * 5秒分の音声を読み取れたら連続失敗を数え直す。10回続けて失敗したら `Failed(MicrophoneUnavailable)` を流して完了する。
 */
internal class AndroidVoskSpeechRecognitionRepository(
    private val transcriberFactory: SpeechTranscriberFactory,
    private val recorderFactory: () -> PcmRecorder,
    private val permissionRepository: RecordAudioPermissionRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SpeechRecognitionRepository {
    override fun isAvailable(): Boolean = transcriberFactory.isAvailable()

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> =
        flow {
            if (!permissionRepository.isGranted()) {
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied))
                return@flow
            }
            val transcriber = createTranscriber()
            if (transcriber == null) {
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
            } else {
                transcriber.use { recognizeWithRetry(it) }
            }
        }.catch { e ->
            if (e !is Exception && e !is LinkageError) throw e
            captureErrorTypeOnly(e)
            emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed))
        }.flowOn(ioDispatcher)

    private fun createTranscriber(): SpeechTranscriber? {
        if (!transcriberFactory.isAvailable()) {
            captureError(IllegalStateException("音声認識を利用できません。認識モデルが見つかりません"))
            return null
        }
        return try {
            transcriberFactory.create()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureError(e)
            null
        } catch (e: LinkageError) {
            captureError(e)
            null
        }
    }

    private suspend fun FlowCollector<SpeechRecognitionEvent>.recognizeWithRetry(transcriber: SpeechTranscriber) {
        var failures = 0
        while (true) {
            recognize(transcriber, onHealthy = { failures = 0 })
            failures++
            if (failures >= VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES) {
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable))
                return
            }
            delay(retryDelayMillis(failures))
        }
    }

    private fun retryDelayMillis(failures: Int): Long {
        var delayMillis = VOSK_RETRY_INITIAL_DELAY_MILLIS
        repeat(failures - 1) { delayMillis = minOf(delayMillis * 2, VOSK_RETRY_MAX_DELAY_MILLIS) }
        return delayMillis
    }

    /** マイクを開いて認識し、マイクを開けない・読み取れなくなったら記録して戻る（収集がやむまで戻らない場合もある）。 */
    private suspend fun FlowCollector<SpeechRecognitionEvent>.recognize(
        transcriber: SpeechTranscriber,
        onHealthy: () -> Unit,
    ) {
        val recorder = recorderFactory()
        try {
            try {
                recorder.start()
            } catch (e: Exception) {
                captureError(IllegalStateException("音声認識を続けられません。マイクの取得に失敗しました"))
                return
            }
            emit(SpeechRecognitionEvent.Listening)
            // 読み取りで待っている間に収集がやむと、読み取りが戻るまで止まれない。キャンセルされたら録音を止めて戻させる
            coroutineScope {
                val watcher =
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        try {
                            awaitCancellation()
                        } finally {
                            recorder.interrupt()
                        }
                    }
                try {
                    readChunks(recorder, transcriber, onHealthy)
                } finally {
                    watcher.cancel()
                }
            }
        } finally {
            recorder.close()
        }
    }

    private suspend fun FlowCollector<SpeechRecognitionEvent>.readChunks(
        recorder: PcmRecorder,
        transcriber: SpeechTranscriber,
        onHealthy: () -> Unit,
    ) {
        val buffer = ByteArray(VOSK_READ_CHUNK_BYTES)
        var readCount = 0
        var lastPartial = ""
        while (true) {
            currentCoroutineContext().ensureActive()
            val size = recorder.read(buffer)
            if (size <= 0) {
                captureError(IllegalStateException("音声認識を続けられません。マイクから音声を読み取れません"))
                return
            }
            if (++readCount == VOSK_HEALTHY_READ_COUNT) onHealthy()
            val pcm = buffer.copyOf(size)
            emit(SpeechRecognitionEvent.Level(pcm.pcmLevel()))
            when (val transcript = transcriber.accept(pcm)) {
                is SpeechTranscript.Partial -> {
                    if (transcript.text != lastPartial) {
                        lastPartial = transcript.text
                        emit(SpeechRecognitionEvent.Partial(transcript.text))
                    }
                }

                is SpeechTranscript.Final -> {
                    lastPartial = ""
                    emit(SpeechRecognitionEvent.Recognized(transcript.candidates))
                }

                null -> {}
            }
        }
    }
}
