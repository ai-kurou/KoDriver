package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.model.MicrophoneInputStatus
import kurou.kodriver.domain.repository.MicrophoneRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveMicrophoneInputStatusUseCaseTest {
    private val repository: MicrophoneRepository = mockk()

    private fun pcm(vararg samples: Int): ByteArray =
        ByteArray(samples.size * 2).also { bytes ->
            samples.forEachIndexed { i, s ->
                bytes[i * 2] = (s and 0xFF).toByte()
                bytes[i * 2 + 1] = (s shr 8).toByte()
            }
        }

    @Test
    fun `開始直後にStartingを流し全ゼロの断片は音量0になる`() =
        runTest {
            every { repository.observeAudio(null) } returns flowOf(MicrophoneAudioEvent.Chunk(pcm(0, 0, 0)))

            val result = ObserveMicrophoneInputStatusUseCase(repository)().toList()

            assertEquals(listOf(MicrophoneInputStatus.Starting, MicrophoneInputStatus.Capturing(0f)), result)
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }

    @Test
    fun `最大振幅の断片は音量1になる`() =
        runTest {
            every { repository.observeAudio(null) } returns flowOf(MicrophoneAudioEvent.Chunk(pcm(-32768, -32768)))

            val result = ObserveMicrophoneInputStatusUseCase(repository)().toList()

            assertEquals(MicrophoneInputStatus.Capturing(1f), result.last())
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }

    @Test
    fun `半分の振幅の断片は音量0点5になり符号に関わらず同じ値になる`() =
        runTest {
            every { repository.observeAudio(null) } returns
                flowOf(MicrophoneAudioEvent.Chunk(pcm(16384, -16384)))

            val result = ObserveMicrophoneInputStatusUseCase(repository)().toList()

            assertEquals(MicrophoneInputStatus.Capturing(0.5f), result.last())
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }

    @Test
    fun `振幅の異なる断片は二乗平均平方根で音量を計算し下位バイトも読む`() =
        runTest {
            every { repository.observeAudio(null) } returns
                flowOf(MicrophoneAudioEvent.Chunk(pcm(0, 32767)), MicrophoneAudioEvent.Chunk(pcm(0, -32768)))

            val levels =
                ObserveMicrophoneInputStatusUseCase(repository)()
                    .toList()
                    .filterIsInstance<MicrophoneInputStatus.Capturing>()
                    .map { it.level }

            // 平均絶対値なら約0.5になるが、二乗平均平方根では約0.7071になる。
            assertEquals(2, levels.size)
            assertEquals(0.707085f, levels[0], absoluteTolerance = 0.0001f)
            assertEquals(0.707107f, levels[1], absoluteTolerance = 0.0001f)
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }

    @Test
    fun `空の断片と端数バイトだけの断片は音量0になる`() =
        runTest {
            every { repository.observeAudio(null) } returns
                flowOf(MicrophoneAudioEvent.Chunk(byteArrayOf()), MicrophoneAudioEvent.Chunk(byteArrayOf(127)))

            val result = ObserveMicrophoneInputStatusUseCase(repository)().toList()

            assertEquals(
                listOf(
                    MicrophoneInputStatus.Starting,
                    MicrophoneInputStatus.Capturing(0f),
                    MicrophoneInputStatus.Capturing(0f),
                ),
                result,
            )
            verify(exactly = 1) { repository.observeAudio(null) }
            confirmVerified(repository)
        }

    @Test
    fun `失敗イベントは理由つきのFailedに変換し指定デバイスで取得する`() =
        runTest {
            val device = AudioInputDevice("マイク A")
            every { repository.observeAudio(device) } returns
                flowOf(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal))

            val result = ObserveMicrophoneInputStatusUseCase(repository)(device).toList()

            assertEquals(
                listOf(
                    MicrophoneInputStatus.Starting,
                    MicrophoneInputStatus.Failed(MicrophoneFailureReason.NoSignal),
                ),
                result,
            )
            verify(exactly = 1) { repository.observeAudio(device) }
            confirmVerified(repository)
        }
}
