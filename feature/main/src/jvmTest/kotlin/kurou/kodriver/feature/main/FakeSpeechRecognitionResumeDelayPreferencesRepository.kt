package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT
import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository

/** 読み上げ後の再開待ち時間をメモリ上に保持するテスト用の Repository。 */
internal class FakeSpeechRecognitionResumeDelayPreferencesRepository(
    initial: Int = SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT,
    var readError: Throwable? = null,
) : SpeechRecognitionResumeDelayPreferencesRepository {
    val saved = MutableStateFlow(initial)

    override fun resumeDelayMs(): Flow<Int> = readError?.let { error -> flow { throw error } } ?: saved

    override suspend fun saveResumeDelayMs(delayMs: Int) {
        saved.value = delayMs
    }
}
