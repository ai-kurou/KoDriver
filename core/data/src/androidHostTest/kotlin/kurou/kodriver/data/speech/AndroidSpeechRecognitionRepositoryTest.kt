@file:Suppress("FunctionNaming")

package kurou.kodriver.data.speech

import android.speech.SpeechRecognizer
import kotlinx.coroutines.CancellationException
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSpeechRecognitionRepositoryTest {
    private val permissionRepository = FakeRecordAudioPermissionRepository()
    private var available = true
    private val engines = ArrayDeque<FakeSpeechRecognitionEngine>()
    private var createdEngineCount = 0
    private val requestedPreferOnDevice = mutableListOf<Boolean>()

    private fun TestScope.createRepository() =
        AndroidSpeechRecognitionRepository(
            engineFactory = { preferOnDevice ->
                createdEngineCount++
                requestedPreferOnDevice += preferOnDevice
                engines.removeFirst()
            },
            recognitionAvailabilityChecker = { available },
            permissionRepository = permissionRepository,
            mainDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun `isAvailableは利用可否チェッカーの結果を返す`() =
        runTest {
            val repository = createRepository()

            assertTrue(repository.isAvailable())
            available = false
            assertFalse(repository.isAvailable())
        }

    @Test
    fun `権限がない場合は権限未許可を通知して完了する`() =
        runTest {
            permissionRepository.granted = false

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied)),
                createRepository().observeRecognition().toList(),
            )
            assertEquals(0, createdEngineCount)
        }

    @Test
    fun `音声認識を利用できない場合は利用不可を通知して完了する`() =
        runTest {
            available = false

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)),
                createRepository().observeRecognition().toList(),
            )
            assertEquals(0, createdEngineCount)
        }

    @Test
    fun `準備完了と認識途中と確定結果を流し結果後に聞き取りを再開する`() =
        runTest {
            val first =
                FakeSpeechRecognitionEngine { listener ->
                    listener.onReady()
                    listener.onPartial("燃")
                    listener.onResults(listOf("燃料残量", "燃料"))
                }
            val second = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            engines.addAll(listOf(first, second))

            val events = createRepository().observeRecognition().take(4).toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Partial("燃"),
                    SpeechRecognitionEvent.Recognized(listOf("燃料残量", "燃料")),
                    SpeechRecognitionEvent.Listening,
                ),
                events,
            )
            assertEquals(SPEECH_RECOGNITION_RESTART_AFTER_RESULT_DELAY_MS, currentTime)
            assertEquals(1, first.startCount)
            assertEquals(1, first.stopCount)
            assertEquals(1, second.startCount)
            assertEquals(1, second.stopCount)
        }

    @Test
    fun `空白の認識途中と空の確定結果は流さず再開する`() =
        runTest {
            val first =
                FakeSpeechRecognitionEngine { listener ->
                    listener.onPartial(" ")
                    listener.onResults(listOf("", " "))
                }
            val second = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            engines.addAll(listOf(first, second))

            val events = createRepository().observeRecognition().take(1).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(2, createdEngineCount)
        }

    @Test
    fun `認識不能のエラーは短い待ち時間で再開する`() =
        runTest {
            engines.addAll(
                listOf(
                    FakeSpeechRecognitionEngine { listener -> listener.onError(SpeechRecognizer.ERROR_NO_MATCH) },
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            val events = createRepository().observeRecognition().take(1).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(SPEECH_RECOGNITION_QUICK_RESTART_DELAY_MS, currentTime)
        }

    @Test
    fun `一時的なエラーは長めの待ち時間で再開する`() =
        runTest {
            engines.addAll(
                listOf(
                    FakeSpeechRecognitionEngine { listener ->
                        listener.onError(SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
                    },
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            val events = createRepository().observeRecognition().take(1).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(SPEECH_RECOGNITION_RETRY_DELAY_MS, currentTime)
        }

    @Test
    fun `続行できないエラーは停止理由を通知して完了する`() =
        runTest {
            val engine =
                FakeSpeechRecognitionEngine { listener ->
                    listener.onReady()
                    listener.onError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
                }
            engines.add(engine)

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied),
                ),
                events,
            )
            assertEquals(1, engine.stopCount)
        }

    @Test
    fun `エンジンの開始に失敗した場合は利用不可を通知して完了する`() =
        runTest {
            val engine = FakeSpeechRecognitionEngine(startFailure = IllegalStateException("開始失敗"))
            engines.add(engine)

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)),
                events,
            )
            assertEquals(1, engine.stopCount)
        }

    @Test
    fun `エンジンの開始中のキャンセルは握りつぶさず伝える`() =
        runTest {
            val engine = FakeSpeechRecognitionEngine(startFailure = CancellationException("キャンセル"))
            engines.add(engine)

            assertFailsWith<CancellationException> { createRepository().observeRecognition().toList() }
            assertEquals(1, engine.stopCount)
        }

    @Test
    fun `収集を終えると聞き取りを止めてエンジンを解放する`() =
        runTest {
            val engine = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            engines.add(engine)

            createRepository().observeRecognition().take(1).toList()

            assertEquals(1, engine.startCount)
            assertEquals(1, engine.stopCount)
        }

    @Test
    fun `認識途中の文字列は最新の1件だけを流す`() =
        runTest {
            engines.add(
                FakeSpeechRecognitionEngine { listener ->
                    repeat(1000) { listener.onPartial("途中$it") }
                    listener.onResults(listOf("確定"))
                },
            )

            val events = createRepository().observeRecognition().take(2).toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Partial("途中999"),
                    SpeechRecognitionEvent.Recognized(listOf("確定")),
                ),
                events,
            )
        }

    @Test
    fun `コールバックが来ないセッションは打ち切って再開する`() =
        runTest {
            val stalled = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            val next = FakeSpeechRecognitionEngine { listener -> listener.onReady() }
            engines.addAll(listOf(stalled, next))

            val events = createRepository().observeRecognition().take(2).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening, SpeechRecognitionEvent.Listening), events)
            assertEquals(SPEECH_RECOGNITION_CALLBACK_TIMEOUT_MS + SPEECH_RECOGNITION_RETRY_DELAY_MS, currentTime)
            assertEquals(1, stalled.stopCount)
        }

    @Test
    fun `端末内認識が言語エラーになった場合は通常の認識に切り替えて直ちに再開する`() =
        runTest {
            val onDevice =
                FakeSpeechRecognitionEngine(isOnDevice = true) { listener ->
                    listener.onError(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
                }
            val standard = FakeSpeechRecognitionEngine(isOnDevice = false) { listener -> listener.onReady() }
            engines.addAll(listOf(onDevice, standard))

            val events = createRepository().observeRecognition().take(1).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(listOf(true, false), requestedPreferOnDevice)
            assertEquals(0, currentTime)
            assertEquals(1, onDevice.stopCount)
        }

    @Test
    fun `切り替えた後は以降のセッションも通常の認識を使う`() =
        runTest {
            engines.addAll(
                listOf(
                    FakeSpeechRecognitionEngine(isOnDevice = true) { listener ->
                        listener.onError(SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED)
                    },
                    FakeSpeechRecognitionEngine { listener -> listener.onResults(listOf("確定")) },
                    FakeSpeechRecognitionEngine { listener -> listener.onReady() },
                ),
            )

            createRepository().observeRecognition().take(2).toList()

            assertEquals(listOf(true, false, false), requestedPreferOnDevice)
        }

    @Test
    fun `通常の認識でも言語エラーになった場合は利用不可を通知して完了する`() =
        runTest {
            engines.addAll(
                listOf(
                    FakeSpeechRecognitionEngine(isOnDevice = true) { listener ->
                        listener.onError(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
                    },
                    FakeSpeechRecognitionEngine(isOnDevice = false) { listener ->
                        listener.onError(SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE)
                    },
                ),
            )

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)),
                events,
            )
            assertEquals(listOf(true, false), requestedPreferOnDevice)
        }

    @Test
    fun `端末内認識でも権限不足は切り替えずに権限未許可を通知して完了する`() =
        runTest {
            engines.add(
                FakeSpeechRecognitionEngine(isOnDevice = true) { listener ->
                    listener.onError(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
                },
            )

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied)),
                events,
            )
            assertEquals(listOf(true), requestedPreferOnDevice)
        }
}
