package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.repository.MicrophoneRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveMicrophoneAudioUseCaseTest {
    private val repository: MicrophoneRepository = mockk()
    private val events =
        listOf(
            MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2)),
            MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal),
        )

    @Test
    fun `指定したデバイスの音声イベントをそのまま流す`() =
        runTest {
            val device = AudioInputDevice("マイク A")
            every { repository.observeAudio(device) } returns events.asFlow()

            assertEquals(events, ObserveMicrophoneAudioUseCase(repository)(device).toList())
            verify(exactly = 1) { repository.observeAudio(device) }
            confirmVerified(repository)
        }

    @Test
    fun `デバイスを省略すると既定のデバイスで取得する`() =
        runTest {
            every { repository.observeAudio(null) } returns events.asFlow()

            assertEquals(events, ObserveMicrophoneAudioUseCase(repository)().toList())
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }
}
