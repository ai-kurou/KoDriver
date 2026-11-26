@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionEvent
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionMode
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val SILENT_CHUNK = ByteArray(VOSK_READ_CHUNK_BYTES)

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class AndroidVoskSpeechRecognitionRepositoryTest {
    private val permissionRepository = FakeRecordAudioPermissionRepository()
    private val transcriberFactory = FakeSpeechTranscriberFactory()
    private val recorder = FakePcmRecorder()
    private val cutQuietSound = FakeMicrophoneCutQuietSoundPreferencesRepository()

    private fun TestScope.createRepository() =
        AndroidVoskSpeechRecognitionRepository(
            transcriberFactory = transcriberFactory,
            recorderFactory = { recorder },
            permissionRepository = permissionRepository,
            microphoneCutQuietSound = cutQuietSound,
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
    fun `マイクを開く処理がキャンセル例外を投げたらマイク取得失敗として再試行せず再スローする`() =
        runTest {
            recorder.startError = CancellationException("cancelled")

            val events = createRepository().observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed)),
                events,
            )
            assertEquals(1, recorder.startCount)
            assertEquals(1, recorder.closeCount)
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
    fun `小さい音をカットの設定に従って小さい断片を無音にし、0なら元の音声を渡す`() =
        runTest {
            val quiet = ByteArray(VOSK_READ_CHUNK_BYTES) { i -> if (i % 2 == 0) 60 else 0 }

            suspend fun acceptedPcm(): ByteArray {
                transcriberFactory.acceptedPcm.clear()
                recorder.chunks.add(quiet)
                transcriberFactory.transcripts.add(SpeechTranscript.Partial("燃料"))
                createRepository().observeRecognition().take(3).toList()
                return transcriberFactory.acceptedPcm.single()
            }

            val withDefault = acceptedPcm()
            cutQuietSound.level.value = 0
            val withOff = acceptedPcm()

            assertContentEquals(ByteArray(quiet.size), withDefault)
            assertContentEquals(quiet, withOff)
        }

    @Test
    fun `聞き取りを続けたまま小さい音をカットの設定を変えると、再開せずに次の断片から反映する`() =
        runBlocking {
            val quiet = ByteArray(VOSK_READ_CHUNK_BYTES) { i -> if (i % 2 == 0) 60 else 0 }
            // 実スレッドで動かすため、収集をやめるまで小さい断片を読み取らせ続ける
            recorder.beforeRead = {
                if (recorder.chunks.isEmpty()) recorder.chunks.add(quiet)
                Thread.sleep(1)
            }
            val repository =
                AndroidVoskSpeechRecognitionRepository(
                    transcriberFactory = transcriberFactory,
                    recorderFactory = { recorder },
                    permissionRepository = permissionRepository,
                    microphoneCutQuietSound = cutQuietSound,
                    ioDispatcher = Dispatchers.IO,
                )
            val job = launch { repository.observeRecognition().collect {} }
            try {
                withTimeout(WAIT_MILLIS) { while (transcriberFactory.acceptedPcm.isEmpty()) delay(10) }
                cutQuietSound.level.value = 0
                withTimeout(WAIT_MILLIS) {
                    while (transcriberFactory.acceptedPcm.none { pcm -> pcm.any { it.toInt() != 0 } }) delay(10)
                }
            } finally {
                job.cancel()
                job.join()
            }

            // 設定を変える前の断片は無音、変えたあとの断片は元の音声で、マイクは開き直していない
            assertContentEquals(ByteArray(quiet.size), transcriberFactory.acceptedPcm.first())
            assertContentEquals(quiet, transcriberFactory.acceptedPcm.first { pcm -> pcm.any { it.toInt() != 0 } })
            assertEquals(1, recorder.startCount)
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
    fun `モードを変えると次の音声から文字起こしへ反映し、同じモードでは重ねて切り替えない`() =
        runTest {
            val repository = createRepository()
            recorder.chunks.addAll(listOf(SILENT_CHUNK, SILENT_CHUNK, SILENT_CHUNK, SILENT_CHUNK))
            var reads = 0
            recorder.beforeRead = {
                reads++
                when (reads) {
                    2 -> repository.setMode(SpeechRecognitionMode.Command)
                    3 -> repository.setMode(SpeechRecognitionMode.Command)
                    4 -> repository.setMode(SpeechRecognitionMode.WakeWord)
                }
            }

            repository.observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.WakeWord),
                transcriberFactory.modes,
            )
        }

    @Test
    fun `開始前に設定したモードは聞き取りの開始で上書きしない`() =
        runTest {
            val repository = createRepository()
            repository.setMode(SpeechRecognitionMode.Command)
            recorder.chunks.add(SILENT_CHUNK)

            repository.observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionMode.Command), transcriberFactory.modes)
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

    @Test
    fun `収集をやめると読み取りで待っているマイクを止めて戻させる`() =
        runTest {
            recorder.chunks.addAll(listOf(SILENT_CHUNK, SILENT_CHUNK))

            createRepository().observeRecognition().first()

            assertEquals(1, recorder.interruptCount)
        }

    @Test
    fun `読み取りで待っている最中に収集をやめると、マイクを止めて読み取りを戻してから閉じる`() =
        runBlocking {
            val blocking = BlockingPcmRecorder()
            val repository =
                AndroidVoskSpeechRecognitionRepository(
                    transcriberFactory = transcriberFactory,
                    recorderFactory = { blocking },
                    permissionRepository = permissionRepository,
                    microphoneCutQuietSound = cutQuietSound,
                    ioDispatcher = Dispatchers.IO,
                )
            val job = launch { repository.observeRecognition().collect {} }
            try {
                withTimeout(WAIT_MILLIS) { blocking.readStarted.await() }

                job.cancel()
                withTimeout(WAIT_MILLIS) { job.join() }
            } finally {
                // 不具合で interrupt() が呼ばれなくても、テストが止まり続けないよう必ず解放する
                blocking.release()
            }

            assertEquals(listOf("interrupt", "close"), blocking.calls.toList())
        }
}

private const val WAIT_MILLIS = 5_000L

/** [read] は [interrupt] が呼ばれるまで戻らず、読み取りで固まったマイクを再現する。 */
private class BlockingPcmRecorder : PcmRecorder {
    val readStarted = CompletableDeferred<Unit>()
    val calls: MutableList<String> = Collections.synchronizedList(mutableListOf())
    private val released = CountDownLatch(1)

    override fun start() = Unit

    override fun read(buffer: ByteArray): Int {
        readStarted.complete(Unit)
        released.await()
        return 0
    }

    override fun interrupt() {
        calls.add("interrupt")
        released.countDown()
    }

    fun release() = released.countDown()

    override fun close() {
        calls.add("close")
    }
}

private class FakeSpeechTranscriberFactory : SpeechTranscriberFactory {
    var available = true
    var createError: Exception? = null
    var linkError: LinkageError? = null
    var acceptError: Exception? = null
    val transcripts = ArrayDeque<SpeechTranscript?>()
    var closeCount = 0
    val modes = mutableListOf<SpeechRecognitionMode>()
    val acceptedPcm = CopyOnWriteArrayList<ByteArray>()

    override fun isAvailable(): Boolean = available

    override fun create(): SpeechTranscriber {
        createError?.let { throw it }
        linkError?.let { throw it }
        return object : SpeechTranscriber {
            override fun accept(pcm: ByteArray): SpeechTranscript? {
                acceptedPcm += pcm.copyOf()
                acceptError?.let { throw it }
                return transcripts.removeFirstOrNull()
            }

            override fun setMode(mode: SpeechRecognitionMode) {
                modes += mode
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
    var interruptCount = 0

    /** 開くたびに実行する。セッションごとに [chunks] を補充するために使う。 */
    var onStart: FakePcmRecorder.() -> Unit = {}

    override fun start() {
        startCount++
        startError?.let { throw it }
        if (startCount <= startFailures) throw IllegalStateException("mic busy")
        onStart()
    }

    /** 読み取りのたびに、読み取る前に実行する。読み取りの合間にモードを切り替えるために使う。 */
    var beforeRead: () -> Unit = {}

    override fun read(buffer: ByteArray): Int {
        beforeRead()
        val chunk = chunks.removeFirstOrNull() ?: return 0
        chunk.copyInto(buffer)
        return chunk.size
    }

    override fun interrupt() {
        interruptCount++
    }

    override fun close() {
        closeCount++
    }
}
