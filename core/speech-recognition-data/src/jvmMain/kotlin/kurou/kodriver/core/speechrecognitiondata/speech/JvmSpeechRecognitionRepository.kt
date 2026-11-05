package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * 選択中のマイクの音声を [SpeechTranscriber] に渡して常時聞き取りを行う、デスクトップ版の [SpeechRecognitionRepository]。
 *
 * 認識モデルが配置されていない・読み込めないときは利用不可として扱う。
 */
internal class JvmSpeechRecognitionRepository(
    private val microphone: MicrophoneRepository,
    private val selectedDevicePreferences: SelectedAudioInputDevicePreferencesRepository,
    private val transcriberFactory: SpeechTranscriberFactory,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpeechRecognitionRepository {
    override fun isAvailable(): Boolean = transcriberFactory.isAvailable()

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> =
        flow {
            val transcriber = createTranscriber()
            if (transcriber == null) {
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
            } else {
                transcriber.use { recognize(it) }
            }
        }.flowOn(computeDispatcher)

    private fun createTranscriber(): SpeechTranscriber? {
        if (!transcriberFactory.isAvailable()) return null
        return try {
            transcriberFactory.create()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureError(e)
            null
        } catch (e: UnsatisfiedLinkError) {
            // 対応していないOS向けのネイティブライブラリが読み込めない場合
            captureError(e)
            null
        }
    }

    private suspend fun FlowCollector<SpeechRecognitionEvent>.recognize(transcriber: SpeechTranscriber) {
        val name = selectedDevicePreferences.selectedDeviceName().first()
        val device = name.takeIf { it != SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED }?.let(::AudioInputDevice)
        emit(SpeechRecognitionEvent.Listening)
        var lastPartial = ""
        microphone.observeAudio(device).collect { event ->
            when (event) {
                is MicrophoneAudioEvent.Chunk -> {
                    when (val transcript = transcriber.accept(event.pcm)) {
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

                is MicrophoneAudioEvent.Failed -> {
                    emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
                }
            }
        }
    }
}
