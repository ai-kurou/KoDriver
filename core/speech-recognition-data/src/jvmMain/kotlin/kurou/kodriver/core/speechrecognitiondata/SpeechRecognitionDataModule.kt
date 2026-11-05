package kurou.kodriver.core.speechrecognitiondata

import kurou.kodriver.core.speechrecognitiondata.device.JvmRecordAudioPermissionRepository
import kurou.kodriver.core.speechrecognitiondata.microphone.JvmAudioInputDeviceRepository
import kurou.kodriver.core.speechrecognitiondata.microphone.JvmMicrophoneRepository
import kurou.kodriver.core.speechrecognitiondata.speech.JvmSpeechRecognitionRepository
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import org.koin.dsl.module

/**
 * マイク入力・音声認識・マイク権限のRepositoryバインドを行うKoinモジュール（:core:speech-recognition-data / jvmMain）。
 *
 * デスクトップ版は音声認識を提供しない（権限は常に許可済み、認識は常に利用不可）。
 * 代わりにJava Soundでマイクのデバイス一覧と音声取得を提供する。Android版はandroidMainの同名モジュールを参照。
 */
val speechRecognitionDataModule =
    module {
        single<RecordAudioPermissionRepository> { JvmRecordAudioPermissionRepository() }
        single<SpeechRecognitionRepository> { JvmSpeechRecognitionRepository() }
        single<AudioInputDeviceRepository> { JvmAudioInputDeviceRepository() }
        single<MicrophoneRepository> { JvmMicrophoneRepository() }
    }
