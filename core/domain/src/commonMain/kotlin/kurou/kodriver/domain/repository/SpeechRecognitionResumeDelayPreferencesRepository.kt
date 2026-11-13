package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 読み上げが終わってから音声認識の聞き取りを再開するまでの待ち時間を保存するRepository。
 */
interface SpeechRecognitionResumeDelayPreferencesRepository {
    /** 保存済みの待ち時間(ミリ秒)。未保存なら `SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT`。 */
    fun resumeDelayMs(): Flow<Int>

    suspend fun saveResumeDelayMs(delayMs: Int)
}
