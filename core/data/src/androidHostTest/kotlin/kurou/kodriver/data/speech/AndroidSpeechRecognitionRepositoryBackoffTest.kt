@file:Suppress("FunctionNaming")

package kurou.kodriver.data.speech

import android.speech.SpeechRecognizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSpeechRecognitionRepositoryBackoffTest {
    private val permissionRepository = FakeRecordAudioPermissionRepository()
    private val engines = ArrayDeque<FakeSpeechRecognitionEngine>()
    private var createdEngineCount = 0

    private fun TestScope.createRepository() =
        AndroidSpeechRecognitionRepository(
            engineFactory = {
                createdEngineCount++
                engines.removeFirst()
            },
            recognitionAvailabilityChecker = { true },
            permissionRepository = permissionRepository,
            mainDispatcher = StandardTestDispatcher(testScheduler),
        )

    private fun failing(errorCode: Int = SpeechRecognizer.ERROR_NETWORK) =
        FakeSpeechRecognitionEngine { listener -> listener.onError(errorCode) }

    @Test
    fun `一時的な失敗が続くと再開の待ち時間を倍々に延ばす`() =
        runTest {
            engines.addAll(
                listOf(
                    failing(),
                    failing(),
                    failing(),
                    FakeSpeechRecognitionEngine { listener ->
                        listener.onReady()
                    },
                ),
            )

            val events = createRepository().observeRecognition().take(1).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(1_000L + 2_000L + 4_000L, currentTime)
        }

    @Test
    fun `再開の待ち時間は上限で止まる`() =
        runTest {
            val failureCount = 8
            engines.addAll(List(failureCount) { failing() })
            engines.add(FakeSpeechRecognitionEngine { listener -> listener.onReady() })

            createRepository().observeRecognition().take(1).toList()

            val expected = (1..failureCount).sumOf { speechRecognitionRetryDelayMs(it) }
            assertEquals(expected, currentTime)
            assertEquals(1_000L + 2_000L + 4_000L + 8_000L + 16_000L + 30_000L * 3, currentTime)
        }

    @Test
    fun `一時的な失敗が上限回数続いたら利用不可を通知して完了する`() =
        runTest {
            engines.addAll(List(SPEECH_RECOGNITION_RETRY_LIMIT) { failing() })

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)),
                events,
            )
            assertEquals(SPEECH_RECOGNITION_RETRY_LIMIT, createdEngineCount)
            assertEquals(
                (1 until SPEECH_RECOGNITION_RETRY_LIMIT).sumOf { speechRecognitionRetryDelayMs(it) },
                currentTime,
            )
        }

    @Test
    fun `認識結果が得られたら連続失敗を数え直す`() =
        runTest {
            engines.addAll(
                listOf(
                    failing(),
                    failing(),
                    FakeSpeechRecognitionEngine { listener -> listener.onResults(listOf("確定")) },
                    failing(),
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            createRepository().observeRecognition().take(2).toList()

            // 失敗2回(1秒+2秒)→結果(0.1秒)→失敗1回目に戻って1秒。
            assertEquals(
                1_000L + 2_000L + SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS + 1_000L,
                currentTime,
            )
        }

    @Test
    fun `無音で終わったら連続失敗を数え直す`() =
        runTest {
            engines.addAll(
                listOf(
                    failing(),
                    failing(),
                    failing(SpeechRecognizer.ERROR_SPEECH_TIMEOUT),
                    failing(),
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            createRepository().observeRecognition().take(1).toList()

            assertEquals(1_000L + 2_000L + SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS + 1_000L, currentTime)
        }

    @Test
    fun `端末内認識から通常の認識への切り替えは連続失敗を数え直さない`() =
        runTest {
            engines.addAll(
                listOf(
                    failing(),
                    FakeSpeechRecognitionEngine(isOnDevice = true) { listener ->
                        listener.onError(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
                    },
                    failing(),
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            createRepository().observeRecognition().take(1).toList()

            // 失敗1回目(1秒)→切り替え(待たない)→失敗2回目(2秒)。
            assertEquals(1_000L + 2_000L, currentTime)
        }
}
