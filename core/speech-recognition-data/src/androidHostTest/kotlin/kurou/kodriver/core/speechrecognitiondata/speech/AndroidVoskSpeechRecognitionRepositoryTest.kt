@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val SILENT_CHUNK = ByteArray(VOSK_READ_CHUNK_BYTES)

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidVoskSpeechRecognitionRepositoryTest {
    private val permissionRepository = FakeRecordAudioPermissionRepository()
    private val transcriberFactory = FakeSpeechTranscriberFactory()
    private val recorder = FakePcmRecorder()

    private fun TestScope.createRepository() =
        AndroidVoskSpeechRecognitionRepository(
            transcriberFactory = transcriberFactory,
            recorderFactory = { recorder },
            permissionRepository = permissionRepository,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

    @Test
    fun `認識モデルがあれば利用できる`() =
        runTest {
            assertTrue(createRepository().isAvailable())
            transcriberFactory.available = false
            assertFalse(createRepository().isAvailable())
        }

    @Test
    fun `権限がなければ権限不足で失敗する`() =
        runTest {
            permissionRepository.granted = false

            val events = createRepository().observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.PermissionDenied)), events)
            assertEquals(0, recorder.startCount)
        }

    @Test
    fun `認識モデルがなければ利用不可で失敗する`() =
        runTest {
            transcriberFactory.available = false

            val events = createRepository().observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
            assertEquals(0, recorder.startCount)
        }

    @Test
    fun `認識モデルを読み込めなければ利用不可で失敗する`() =
        runTest {
            transcriberFactory.createError = IllegalStateException("model")

            val events = createRepository().observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
        }

    @Test
    fun `対応外のネイティブライブラリで読み込めなければ利用不可で失敗する`() =
        runTest {
            transcriberFactory.linkError = UnsatisfiedLinkError("jna")

            val events = createRepository().observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
        }

    @Test
    fun `マイクを開けない失敗が10回続いたら待ち時間を延ばして再開したあとマイク取得失敗で失敗する`() =
        runTest {
            recorder.startError = IllegalStateException("mic")

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable)),
                events,
            )
            assertEquals(VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES, recorder.startCount)
            assertEquals(VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES, recorder.closeCount)
            // 待ち時間は 1, 2, 4, 8, 16 秒と延び、上限の30秒で頭打ちになる（9回待つ）
            assertEquals(151_000L, testScheduler.currentTime)
        }

    @Test
    fun `マイクを開けない失敗が途中で解消したら聞き取りを再開する`() =
        runTest {
            recorder.startFailures = 3
            recorder.chunks.add(SILENT_CHUNK)

            val events = createRepository().observeRecognition().take(2).toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening, SpeechRecognitionEvent.Level(0f)), events)
            assertEquals(4, recorder.startCount)
            assertEquals(7_000L, testScheduler.currentTime)
        }

    @Test
    fun `5秒分の音声を読み取れたら連続失敗を数え直す`() =
        runTest {
            recorder.onStart = { repeat(VOSK_HEALTHY_READ_COUNT) { chunks.add(SILENT_CHUNK) } }

            val listening =
                createRepository()
                    .observeRecognition()
                    .filterIsInstance<SpeechRecognitionEvent.Listening>()
                    .take(VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES + 2)
                    .toList()

            assertEquals(VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES + 2, listening.size)
        }

    @Test
    fun `聞き取りを始めると音量と認識途中と確定結果を流す`() =
        runTest {
            recorder.chunks.addAll(listOf(SILENT_CHUNK, SILENT_CHUNK, SILENT_CHUNK, SILENT_CHUNK))
            transcriberFactory.transcripts.addAll(
                listOf(
                    SpeechTranscript.Partial("燃料"),
                    SpeechTranscript.Partial("燃料"),
                    SpeechTranscript.Final(listOf("燃料", "然料")),
                    null,
                ),
            )

            val events = createRepository().observeRecognition().take(7).toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Partial("燃料"),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Recognized(listOf("燃料", "然料")),
                    SpeechRecognitionEvent.Level(0f),
                ),
                events,
            )
        }

    @Test
    fun `確定のあとに同じ文字列の認識途中が来たら再び流す`() =
        runTest {
            recorder.chunks.addAll(listOf(SILENT_CHUNK, SILENT_CHUNK, SILENT_CHUNK))
            transcriberFactory.transcripts.addAll(
                listOf(
                    SpeechTranscript.Partial("燃料"),
                    SpeechTranscript.Final(listOf("燃料")),
                    SpeechTranscript.Partial("燃料"),
                ),
            )

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Partial("燃料"),
                    SpeechRecognitionEvent.Recognized(listOf("燃料")),
                    SpeechRecognitionEvent.Partial("燃料"),
                ),
                events
                    .filter { it !is SpeechRecognitionEvent.Level && it != SpeechRecognitionEvent.Listening }
                    .take(3),
            )
        }

    @Test
    fun `マイクから音声を読み取れない失敗が続いたらマイク取得失敗で失敗する`() =
        runTest {
            recorder.chunks.add(SILENT_CHUNK)

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable),
                events.last(),
            )
            assertEquals(VOSK_MAX_CONSECUTIVE_MICROPHONE_FAILURES, recorder.closeCount)
            assertEquals(1, transcriberFactory.closeCount)
        }

    @Test
    fun `認識中に想定外の例外が出たら認識失敗で失敗する`() =
        runTest {
            recorder.chunks.add(SILENT_CHUNK)
            transcriberFactory.acceptError = IllegalStateException("accept")

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed),
                events.last(),
            )
            assertEquals(1, recorder.closeCount)
        }

    @Test
    fun `収集をやめるとマイクと認識エンジンを閉じる`() =
        runTest {
            recorder.chunks.addAll(listOf(SILENT_CHUNK, SILENT_CHUNK))

            createRepository().observeRecognition().first()

            assertEquals(1, recorder.closeCount)
            assertEquals(1, transcriberFactory.closeCount)
        }
}

private class FakeSpeechTranscriberFactory : SpeechTranscriberFactory {
    var available = true
    var createError: Exception? = null
    var linkError: LinkageError? = null
    var acceptError: Exception? = null
    val transcripts = ArrayDeque<SpeechTranscript?>()
    var closeCount = 0

    override fun isAvailable(): Boolean = available

    override fun create(): SpeechTranscriber {
        createError?.let { throw it }
        linkError?.let { throw it }
        return object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? {
                acceptError?.let { throw it }
                return transcripts.removeFirstOrNull()
            }

            override fun close() {
                closeCount++
            }
        }
    }
}

/** [chunks] を順に返し、尽きたら読み取り失敗（0）を返す。 */
private class FakePcmRecorder : PcmRecorder {
    var startError: Exception? = null

    /** 先頭からこの回数だけ開けず、それ以降は開ける。 */
    var startFailures = 0
    val chunks = ArrayDeque<ByteArray>()
    var startCount = 0
    var closeCount = 0

    /** 開くたびに実行する。セッションごとに [chunks] を補充するために使う。 */
    var onStart: FakePcmRecorder.() -> Unit = {}

    override fun start() {
        startCount++
        startError?.let { throw it }
        if (startCount <= startFailures) throw IllegalStateException("mic busy")
        onStart()
    }

    override fun read(buffer: ByteArray): Int {
        val chunk = chunks.removeFirstOrNull() ?: return 0
        chunk.copyInto(buffer)
        return chunk.size
    }

    override fun close() {
        closeCount++
    }
}
