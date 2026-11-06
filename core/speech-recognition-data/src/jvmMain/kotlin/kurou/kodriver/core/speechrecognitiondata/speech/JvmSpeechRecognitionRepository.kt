package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
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
        }.catch { e ->
            // 設定の読み取りや認識中に想定外の例外が出ても、続行できないことをFailedで知らせて完了する
            if (e !is Exception && e !is LinkageError) throw e
            captureError(e)
            emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed))
        }.flowOn(computeDispatcher)

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
            // 対応していないOS向けのネイティブライブラリが読み込めない場合。初回失敗後のクラス初期化エラー（NoClassDefFoundError）も含む
            captureError(e)
            null
        }
    }

    private fun MicrophoneFailureReason.toSpeechFailureReason(): SpeechRecognitionFailureReason =
        when (this) {
            MicrophoneFailureReason.Unavailable -> SpeechRecognitionFailureReason.MicrophoneUnavailable
            MicrophoneFailureReason.NoSignal -> SpeechRecognitionFailureReason.NoSignal
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
                    // 理由が画面とログから分かるよう、マイク側の失敗理由を保って通知する
                    captureError(
                        IllegalStateException(
                            "音声認識を続けられません。マイクの取得に失敗しました: ${event.reason}（デバイス: ${device?.name ?: "既定"}）",
                        ),
                    )
                    emit(SpeechRecognitionEvent.Failed(event.reason.toSpeechFailureReason()))
                }
            }
        }
    }
}
