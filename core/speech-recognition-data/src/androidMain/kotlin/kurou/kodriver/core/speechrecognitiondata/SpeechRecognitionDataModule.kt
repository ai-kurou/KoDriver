package kurou.kodriver.core.speechrecognitiondata

import android.content.Context
import android.speech.SpeechRecognizer
import kurou.kodriver.core.speechrecognitiondata.device.AndroidRecordAudioPermissionRepository
import kurou.kodriver.core.speechrecognitiondata.speech.AndroidSpeechRecognitionRepository
import kurou.kodriver.core.speechrecognitiondata.speech.PlatformSpeechRecognitionEngine
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import org.koin.dsl.module

/**
 * マイク権限・音声認識のRepositoryバインドを行うKoinモジュール（:core:speech-recognition-data / androidMain）。
 *
 * マイク入力（`MicrophoneRepository`）はデスクトップ専用のためAndroidには登録しない。
 */
val speechRecognitionDataModule =
    module {
        // マイク（RECORD_AUDIO）の権限が許可されているか（音声による手動読み上げの開始可否に使用）
        single<RecordAudioPermissionRepository> { AndroidRecordAudioPermissionRepository(get<Context>()) }
        // マイクからの常時聞き取り（端末内の音声認識を優先して使う）
        single<SpeechRecognitionRepository> {
            val context = get<Context>()
            AndroidSpeechRecognitionRepository(
                engineFactory = { preferOnDevice -> PlatformSpeechRecognitionEngine(context, preferOnDevice) },
                recognitionAvailabilityChecker = { SpeechRecognizer.isRecognitionAvailable(context) },
                permissionRepository = get(),
            )
        }
    }
