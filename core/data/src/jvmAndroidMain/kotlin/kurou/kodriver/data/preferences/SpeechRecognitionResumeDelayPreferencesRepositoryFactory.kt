package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository

/**
 * 読み上げ後の聞き取り再開までの待ち時間の Repository の永続化実装を生成する。
 */
fun createSpeechRecognitionResumeDelayPreferencesRepository(
    directory: String,
): SpeechRecognitionResumeDelayPreferencesRepository =
    SpeechRecognitionResumeDelayPreferencesRepositoryImpl(
        createSpeechRecognitionResumeDelayPreferencesDataStore(directory),
    )
