package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * 選択中のマイクの音声を [SpeechTranscriber] に渡して常時聞き取りを行う、デスクトップ版の [SpeechRecognitionRepository]。
 *
 * 聞き取りは [setMode] のモード（ウェイクワード待ち・コマンド待ち・読み上げ中の停止）に従う。
 * 認識モデルが配置されていない・読み込めないときは利用不可として扱う。
 */
internal class JvmSpeechRecognitionRepository(
    private val microphone: MicrophoneRepository,
    private val audioInputDevices: AudioInputDeviceRepository,
    private val selectedDevicePreferences: SelectedAudioInputDevicePreferencesRepository,
    private val transcriberFactory: SpeechTranscriberFactory,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SpeechRecognitionRepository {
    private val mode = MutableStateFlow(SpeechRecognitionMode.WakeWord)

    override fun isAvailable(): Boolean = transcriberFactory.isAvailable()

    override fun setMode(mode: SpeechRecognitionMode) {
        this.mode.value = mode
    }

    override fun observeRecognition(): Flow<SpeechRecognitionEvent> =
        flow {
            val transcriber = transcriberFactory.createOrNull()
            if (transcriber == null) {
                emit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))
            } else {
                transcriber.use { recognize(it) }
            }
        }.failRecognitionOnError()
            .flowOn(computeDispatcher)

    private fun MicrophoneFailureReason.toSpeechFailureReason(): SpeechRecognitionFailureReason =
        when (this) {
            MicrophoneFailureReason.Unavailable -> SpeechRecognitionFailureReason.MicrophoneUnavailable
            MicrophoneFailureReason.NoSignal -> SpeechRecognitionFailureReason.NoSignal
        }

    /**
     * 画面の選択と同じデバイスを返す。保存済みの名前が一覧にある場合はそれを、一覧にない場合や未選択の場合は
     * 一覧の先頭を使う（画面は同じ規則で選択中のデバイスを表示する）。一覧が空なら null（既定のデバイス）。
     */
    private suspend fun resolveDevice(): AudioInputDevice? {
        val name = selectedDevicePreferences.selectedDeviceName().first()
        // 一覧を取得できなくても既定のデバイスで聞き取りを続ける
        val devices =
            try {
                audioInputDevices.getDevices()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 例外にデバイス名が含まれ得るため、固定の文言だけを記録する
                captureError(IllegalStateException("入力デバイスの一覧を取得できません。既定のマイクを使います"))
                emptyList()
            }
        return devices.firstOrNull { it.name == name } ?: devices.firstOrNull()
    }

    private suspend fun FlowCollector<SpeechRecognitionEvent>.recognize(transcriber: SpeechTranscriber) {
        val device = resolveDevice()
        emit(SpeechRecognitionEvent.Listening)
        val pipeline = RecognitionPipeline(transcriber, mode)
        microphone.observeAudio(device).collect { event ->
            when (event) {
                is MicrophoneAudioEvent.Chunk -> {
                    with(pipeline) { process(event.pcm) }
                }

                is MicrophoneAudioEvent.Failed -> {
                    // 理由が画面から分かるよう、マイク側の失敗理由を保って通知する。
                    // 無音はOSのマイク設定が原因で画面に案内を出すため記録しない。デバイス名は個人情報を含み得るため記録しない。
                    if (event.reason == MicrophoneFailureReason.Unavailable) {
                        captureError(IllegalStateException("音声認識を続けられません。マイクの取得に失敗しました"))
                    }
                    emit(SpeechRecognitionEvent.Failed(event.reason.toSpeechFailureReason()))
                }
            }
        }
    }
}
