@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kotlin.test.Test
import kotlin.test.assertEquals

/** 入力音量が届く間隔（仮想時間）。無通知の打ち切り時間より短い。 */
private const val LEVEL_INTERVAL_MS = 10_000L

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSpeechRecognitionRepositoryLevelTest {
    private val permissionRepository = FakeRecordAudioPermissionRepository()
    private val engines = ArrayDeque<FakeSpeechRecognitionEngine>()

    private fun TestScope.createRepository() =
        AndroidSpeechRecognitionRepository(
            engineFactory = { engines.removeFirst() },
            recognitionAvailabilityChecker = { true },
            permissionRepository = permissionRepository,
            mainDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun `入力音量は最新の1件だけを流す`() =
        runTest {
            engines.add(
                FakeSpeechRecognitionEngine { listener ->
                    repeat(1000) { listener.onLevel(it / 1000f) }
                    listener.onResults(listOf("確定"))
                },
            )

            val events = createRepository().observeRecognition().take(3).toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Level(0.999f),
                    SpeechRecognitionEvent.Recognized(listOf("確定")),
                    SpeechRecognitionEvent.Level(0f),
                ),
                events,
            )
        }

    @Test
    fun `入力音量が届いても無通知の打ち切りまでの時間は延びない`() =
        runTest {
            var stalledListener: SpeechRecognitionEngineListener? = null
            val stalled =
                FakeSpeechRecognitionEngine { listener ->
                    stalledListener = listener
                    listener.onReady()
                }
            val next = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            engines.addAll(listOf(stalled, next))
            launch {
                repeat(2) {
                    delay(LEVEL_INTERVAL_MS)
                    stalledListener?.onLevel(0.5f)
                }
            }

            val events = createRepository().observeRecognition().take(5).toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Level(0.5f),
                    SpeechRecognitionEvent.Level(0.5f),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Listening,
                ),
                events,
            )
            assertEquals(SPEECH_RECOGNITION_CALLBACK_TIMEOUT_MS + SPEECH_RECOGNITION_RETRY_DELAY_MS, currentTime)
            assertEquals(1, stalled.stopCount)
        }
}
