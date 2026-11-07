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
import kurou.kodriver.domain.model.SpeechRecognitionMode
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class JvmSpeechRecognitionRepositoryTest {
    private val preferences = FakeSelectedDevicePreferences()
    private val microphone = FakeMicrophone()
    private val devices = FakeAudioInputDevices()

    private fun repository(factory: SpeechTranscriberFactory) =
        JvmSpeechRecognitionRepository(microphone, devices, preferences, factory, Dispatchers.Unconfined)

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
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Partial("次"),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Recognized(listOf("次へ", "つぎへ")),
                    SpeechRecognitionEvent.Level(0f),
                    SpeechRecognitionEvent.Partial("次"),
                ),
                events,
            )
            assertEquals(5, transcriber.acceptedChunks.size)
            assertTrue(transcriber.closed)
        }

    @Test
    fun `モードを変えると次の音声から文字起こしへ反映し、同じモードでは重ねて切り替えない`() =
        runTest {
            val transcriber = FakeTranscriber()
            val repository = repository(FakeTranscriberFactory(available = true, transcriber = transcriber))
            microphone.events =
                flow {
                    emit(chunk(1))
                    repository.setMode(SpeechRecognitionMode.Command)
                    emit(chunk(2))
                    emit(chunk(3))
                    repository.setMode(SpeechRecognitionMode.Paused)
                    emit(chunk(4))
                    repository.setMode(SpeechRecognitionMode.WakeWord)
                    emit(chunk(5))
                }

            repository.observeRecognition().toList()

            assertEquals(
                listOf(SpeechRecognitionMode.Command, SpeechRecognitionMode.Paused, SpeechRecognitionMode.WakeWord),
                transcriber.modes,
            )
        }

    @Test
    fun `モードを切り替えると前の途中経過を引き継がず同じ途中経過でも流し直す`() =
        runTest {
            val transcriber =
                FakeTranscriber(SpeechTranscript.Partial("次"), SpeechTranscript.Partial("次"))
            val repository = repository(FakeTranscriberFactory(available = true, transcriber = transcriber))
            microphone.events =
                flow {
                    emit(chunk(1))
                    repository.setMode(SpeechRecognitionMode.Command)
                    emit(chunk(2))
                }

            val events = repository.observeRecognition().toList()

            assertEquals(2, events.count { it == SpeechRecognitionEvent.Partial("次") })
        }

    @Test
    fun `聞き取りの開始時はそれまでのモードにかかわらずウェイクワード待ちから始まる`() =
        runTest {
            val transcriber = FakeTranscriber()
            val repository = repository(FakeTranscriberFactory(available = true, transcriber = transcriber))
            repository.setMode(SpeechRecognitionMode.Command)
            microphone.events = flowOf(chunk(1))

            repository.observeRecognition().toList()

            assertEquals(emptyList(), transcriber.modes)
        }

    @Test
    fun `一覧にある保存済みのマイクを使い、未選択や一覧にない場合は先頭、一覧が空なら既定のマイクを使う`() =
        runTest {
            val factory = FakeTranscriberFactory(available = true, transcriber = FakeTranscriber())
            microphone.events = flowOf()
            devices.names = listOf("Mic A", "Mic B")

            preferences.name.update { "Mic B" }
            repository(factory).observeRecognition().toList()
            preferences.name.update { "Mic Z" }
            repository(factory).observeRecognition().toList()
            preferences.name.update { SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED }
            repository(factory).observeRecognition().toList()
            devices.names = emptyList()
            repository(factory).observeRecognition().toList()

            assertEquals(
                listOf(AudioInputDevice("Mic B"), AudioInputDevice("Mic A"), AudioInputDevice("Mic A"), null),
                microphone.requestedDevices,
            )
        }

    @Test
    fun `デバイス一覧を取得できなくても既定のマイクで聞き取りを続ける`() =
        runTest {
            microphone.events = flowOf()
            devices.error = IllegalStateException("enumerate")

            val events = repository(FakeTranscriberFactory(available = true)).observeRecognition().toList()

            assertEquals(listOf(SpeechRecognitionEvent.Listening), events)
            assertEquals(listOf<AudioInputDevice?>(null), microphone.requestedDevices)
        }

    @Test
    fun `音声の断片ごとに音量を流す`() =
        runTest {
            val loud = byteArrayOf(0x00, 0x20, 0x00, 0x20)
            microphone.events = flowOf(MicrophoneAudioEvent.Chunk(loud), MicrophoneAudioEvent.Chunk(ByteArray(4)))

            val events = observeWith(FakeTranscriber())

            val levels = events.filterIsInstance<SpeechRecognitionEvent.Level>().map { it.level }
            // 8192 / 32768 = 0.25 → 約 -12 dBFS → (-12 + 60) / 50 ≒ 0.959
            assertEquals(2, levels.size)
            assertEquals(0.959f, levels[0], absoluteTolerance = 0.001f)
            assertEquals(0f, levels[1])
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
                    SpeechRecognitionEvent.Level(0f),
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

private class FakeAudioInputDevices : AudioInputDeviceRepository {
    var names: List<String> = emptyList()
    var error: Throwable? = null

    override suspend fun getDevices(): List<AudioInputDevice> {
        error?.let { throw it }
        return names.map(::AudioInputDevice)
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
    val modes = mutableListOf<SpeechRecognitionMode>()
    var closed = false
        private set

    override fun setMode(mode: SpeechRecognitionMode) {
        modes += mode
    }

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
