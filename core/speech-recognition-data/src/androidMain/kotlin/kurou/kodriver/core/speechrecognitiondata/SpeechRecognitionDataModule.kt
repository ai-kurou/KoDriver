package kurou.kodriver.core.speechrecognitiondata

import android.content.Context
import kurou.kodriver.core.speechrecognitiondata.device.AndroidRecordAudioPermissionRepository
import kurou.kodriver.core.speechrecognitiondata.speech.AndroidSpeechRecognitionChimePlayer
import kurou.kodriver.core.speechrecognitiondata.speech.AndroidVoskSpeechRecognitionRepository
import kurou.kodriver.core.speechrecognitiondata.speech.AndroidVoskSpeechTranscriberFactory
import kurou.kodriver.core.speechrecognitiondata.speech.AudioRecordPcmRecorder
import kurou.kodriver.core.speechrecognitiondata.speech.RegistrySpeechCommandGuideRepository
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository
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
        // マイクからの常時聞き取り。OSの SpeechRecognizer は認識の開始ごとに開始音が鳴るため、
        // 同梱した認識モデル（Vosk）を AudioRecord の音声で使う
        single<SpeechRecognitionRepository> {
            AndroidVoskSpeechRecognitionRepository(
                transcriberFactory = AndroidVoskSpeechTranscriberFactory(get<Context>()),
                recorderFactory = { AudioRecordPcmRecorder() },
                permissionRepository = get(),
            )
        }
        // ウェイクワード検出時の確認音（読み上げの開始音とは別の音）
        single<SpeechRecognitionChimePlayer> { AndroidSpeechRecognitionChimePlayer() }
        single<SpeechCommandGuideRepository> { RegistrySpeechCommandGuideRepository() }
    }
