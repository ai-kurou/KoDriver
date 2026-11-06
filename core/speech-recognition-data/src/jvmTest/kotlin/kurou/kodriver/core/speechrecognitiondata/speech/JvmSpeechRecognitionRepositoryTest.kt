package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmSpeechRecognitionRepositoryTest {
    private val preferences = FakeSelectedDevicePreferences()
    private val microphone = FakeMicrophone()

    private fun repository(factory: SpeechTranscriberFactory) =
        JvmSpeechRecognitionRepository(microphone, preferences, factory, Dispatchers.Unconfined)

    private suspend fun observeWith(transcriber: SpeechTranscriber): List<SpeechRecognitionEvent> =
        repository(FakeTranscriberFactory(available = true, transcriber = transcriber))
            .observeRecognition()
            .toList()

    private fun chunk(vararg value: Byte) = MicrophoneAudioEvent.Chunk(byteArrayOf(*value))

    @Test
    fun `isAvailableは認識モデルの有無に従う`() {
        assertTrue(repository(FakeTranscriberFactory(available = true)).isAvailable())
        assertFalse(repository(FakeTranscriberFactory(available = false)).isAvailable())
    }

    @Test
    fun `認識モデルが無ければ利用不可を通知して完了する`() =
        runTest {
            val factory = FakeTranscriberFactory(available = false)

            val events = repository(factory).observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
            assertEquals(0, factory.createCount)
            assertEquals(emptyList(), microphone.requestedDevices)
        }

    @Test
    fun `認識モデルの読み込みに失敗したら利用不可を通知する`() =
        runTest {
            val factory = FakeTranscriberFactory(available = true, createError = IllegalStateException("model broken"))

            val events = repository(factory).observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
            assertEquals(emptyList(), microphone.requestedDevices)
        }

    @Test
    fun `ネイティブライブラリを読み込めなければ利用不可を通知する`() =
        runTest {
            val factory = FakeTranscriberFactory(available = true, createError = UnsatisfiedLinkError("no native"))

            val events = repository(factory).observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
        }

    @Test
    fun `ネイティブライブラリの初期化エラーでも利用不可を通知する`() =
        runTest {
            val factory = FakeTranscriberFactory(available = true, createError = NoClassDefFoundError("LibVosk"))

            val events = repository(factory).observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable)), events)
        }

    @Test
    fun `途中経過と確定結果を認識イベントとして流し、同じ途中経過は重ねて流さない`() =
        runTest {
            val transcriber =
                FakeTranscriber(
                    SpeechTranscript.Partial("次"),
                    SpeechTranscript.Partial("次"),
                    null,
                    SpeechTranscript.Final(listOf("次へ", "つぎへ")),
                    SpeechTranscript.Partial("次"),
                )
            microphone.events = flowOf(chunk(1), chunk(2), chunk(3), chunk(4), chunk(5))

            val events = observeWith(transcriber)

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Partial("次"),
                    SpeechRecognitionEvent.Recognized(listOf("次へ", "つぎへ")),
                    SpeechRecognitionEvent.Partial("次"),
                ),
                events,
            )
            assertEquals(5, transcriber.acceptedChunks.size)
            assertTrue(transcriber.closed)
        }

    @Test
    fun `保存済みのマイクを使い、未選択なら既定のマイクを使う`() =
        runTest {
            val factory = FakeTranscriberFactory(available = true, transcriber = FakeTranscriber())
            microphone.events = flowOf()

            preferences.name.update { "Mic A" }
            repository(factory).observeRecognition().toList()
            preferences.name.update { SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED }
            repository(factory).observeRecognition().toList()

            assertEquals(listOf(AudioInputDevice("Mic A"), null), microphone.requestedDevices)
        }

    @Test
    fun `マイクで無音が続いたら無音の失敗を通知し、認識エンジンを閉じる`() =
        runTest {
            val transcriber = FakeTranscriber()
            microphone.events = flowOf(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal))

            val events = observeWith(transcriber)

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.NoSignal),
                ),
                events,
            )
            assertTrue(transcriber.closed)
        }

    @Test
    fun `マイクを開けなければマイク利用不可を通知し、認識エンジンを閉じる`() =
        runTest {
            val transcriber = FakeTranscriber()
            microphone.events = flowOf(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable))

            val events = observeWith(transcriber)

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.MicrophoneUnavailable),
                ),
                events,
            )
            assertTrue(transcriber.closed)
        }

    @Test
    fun `認識中に想定外の例外が出たら認識失敗を通知し、認識エンジンを閉じる`() =
        runTest {
            val transcriber = FakeTranscriber().apply { acceptError = IllegalStateException("accept failed") }
            microphone.events = flowOf(chunk(1), chunk(2))

            val events = observeWith(transcriber)

            assertEquals(
                listOf(
                    SpeechRecognitionEvent.Listening,
                    SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed),
                ),
                events,
            )
            assertEquals(1, transcriber.acceptedChunks.size)
            assertTrue(transcriber.closed)
        }

    @Test
    fun `選択マイクの読み取りに失敗したら認識失敗を通知し、認識エンジンを閉じる`() =
        runTest {
            val transcriber = FakeTranscriber()
            preferences.readError = IllegalStateException("read failed")

            val events = observeWith(transcriber)

            assertEquals(
                listOf(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.RecognitionFailed)),
                events,
            )
            assertEquals(emptyList(), microphone.requestedDevices)
            assertTrue(transcriber.closed)
        }

    @Test
    fun `収集をキャンセルすると認識エンジンを閉じる`() =
        runTest {
            val transcriber = FakeTranscriber()
            microphone.events = flow { awaitCancellation() }

            val job = launch(Dispatchers.Unconfined) { observeWith(transcriber) }
            assertFalse(transcriber.closed)
            job.cancelAndJoin()

            assertTrue(transcriber.closed)
        }
}

private class FakeSelectedDevicePreferences : SelectedAudioInputDevicePreferencesRepository {
    val name = MutableStateFlow(SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED)
    var readError: Throwable? = null

    override fun selectedDeviceName(): Flow<String> = readError?.let { error -> flow { throw error } } ?: name

    override suspend fun saveSelectedDeviceName(name: String) {
        this.name.update { name }
    }
}

private class FakeMicrophone : MicrophoneRepository {
    var events: Flow<MicrophoneAudioEvent> = flowOf()
    val requestedDevices = mutableListOf<AudioInputDevice?>()

    override fun observeAudio(device: AudioInputDevice?): Flow<MicrophoneAudioEvent> {
        requestedDevices += device
        return events
    }
}

private class FakeTranscriber(
    vararg transcripts: SpeechTranscript?,
) : SpeechTranscriber {
    private val queue = transcripts.toMutableList()
    val acceptedChunks = mutableListOf<ByteArray>()
    var acceptError: Throwable? = null
    var closed = false
        private set

    override fun accept(pcm: ByteArray): SpeechTranscript? {
        acceptedChunks += pcm
        acceptError?.let { throw it }
        return if (queue.isEmpty()) null else queue.removeAt(0)
    }

    override fun close() {
        closed = true
    }
}

private class FakeTranscriberFactory(
    private val available: Boolean,
    private val transcriber: SpeechTranscriber = FakeTranscriber(),
    private val createError: Throwable? = null,
) : SpeechTranscriberFactory {
    var createCount = 0
        private set

    override fun isAvailable(): Boolean = available

    override fun create(): SpeechTranscriber {
        createCount++
        createError?.let { throw it }
        return transcriber
    }
}
