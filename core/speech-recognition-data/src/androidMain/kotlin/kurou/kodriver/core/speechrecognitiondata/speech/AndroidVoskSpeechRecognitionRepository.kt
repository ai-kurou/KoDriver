package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository

/** 1回に読み取る音声の長さ（16kHz・モノラル・16bit PCM で100ミリ秒分のバイト数）。 */
internal const val VOSK_READ_CHUNK_BYTES = 3_200

/**
 * マイクを [PcmRecorder] で開きっぱなしにして [SpeechTranscriber]（Vosk）で認識する、Android の [SpeechRecognitionRepository]。
 *
 * OSの `SpeechRecognizer` は認識を始めるたびに開始音を鳴らすが、こちらは認識サービスを介さないため鳴らない。
 * 認識モデルは全ビルドに同梱するが、読み込めなかったときは利用不可になる。
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
                transcriber.use { recognize(it) }
            }
        }.catch { e ->
            if (e !is Exception && e !is LinkageError) throw e
            captureError(e)
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

    private suspend fun FlowCollector<SpeechRecognitionEvent>.recognize(transcriber: SpeechTranscriber) {
        val recorder = recorderFactory()
        try {
            try {
                recorder.start()
            } catch (e: Exception) {
                captureError(IllegalStateException("音声認識を続けられません。マイクの取得に失敗しました"))
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable))
                return
            }
            emit(SpeechRecognitionEvent.Listening)
            readChunks(recorder, transcriber)
        } finally {
            recorder.close()
        }
    }

    private suspend fun FlowCollector<SpeechRecognitionEvent>.readChunks(
        recorder: PcmRecorder,
        transcriber: SpeechTranscriber,
    ) {
        val buffer = ByteArray(VOSK_READ_CHUNK_BYTES)
        var lastPartial = ""
        while (true) {
            currentCoroutineContext().ensureActive()
            val size = recorder.read(buffer)
            if (size <= 0) {
                captureError(IllegalStateException("音声認識を続けられません。マイクから音声を読み取れません"))
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable))
                return
            }
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
