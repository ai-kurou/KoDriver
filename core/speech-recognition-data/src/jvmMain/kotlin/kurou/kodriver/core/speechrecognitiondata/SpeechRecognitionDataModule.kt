package kurou.kodriver.core.speechrecognitiondata

import kurou.kodriver.core.speechrecognitiondata.device.JvmRecordAudioPermissionRepository
import kurou.kodriver.core.speechrecognitiondata.microphone.JvmAudioInputDeviceRepository
import kurou.kodriver.core.speechrecognitiondata.microphone.JvmMicrophoneRepository
import kurou.kodriver.core.speechrecognitiondata.speech.JvmSpeechRecognitionRepository
import kurou.kodriver.core.speechrecognitiondata.speech.SpeechTranscriberFactory
import kurou.kodriver.core.speechrecognitiondata.speech.VoskSpeechTranscriberFactory
import kurou.kodriver.core.speechrecognitiondata.speech.resolveVoskModelDirectory
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import org.koin.dsl.module

/** Voskの日本語モデルのディレクトリ。手動配置を優先し、無ければアプリ同梱のモデルを使う。 */
private val voskModelDirectory =
    resolveVoskModelDirectory(
        userHome = System.getProperty("user.home"),
        resourcesDirectory = System.getProperty("compose.application.resources.dir"),
    )

/**
 * マイク入力・音声認識・マイク権限のRepositoryバインドを行うKoinモジュール（:core:speech-recognition-data / jvmMain）。
 *
 * デスクトップ版はJava Soundでマイクのデバイス一覧と音声取得を行い、取得した音声をVoskで認識する（権限は常に許可済み）。
 * 認識モデルが配置されていなければ認識は利用不可になる。Android版はandroidMainの同名モジュールを参照。
 */
val speechRecognitionDataModule =
    module {
        single<RecordAudioPermissionRepository> { JvmRecordAudioPermissionRepository() }
        single<SpeechTranscriberFactory> { VoskSpeechTranscriberFactory(voskModelDirectory) }
        single<SpeechRecognitionRepository> {
            JvmSpeechRecognitionRepository(
                microphone = get(),
                audioInputDevices = get(),
                selectedDevicePreferences = get(),
                transcriberFactory = get(),
            )
        }
        single<AudioInputDeviceRepository> { JvmAudioInputDeviceRepository() }
        single<MicrophoneRepository> { JvmMicrophoneRepository() }
    }
