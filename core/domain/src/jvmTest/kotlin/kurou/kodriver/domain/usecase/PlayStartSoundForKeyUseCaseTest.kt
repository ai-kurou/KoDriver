package kurou.kodriver.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test

class PlayStartSoundForKeyUseCaseTest {
    private val engine: TextToSpeechEngine = mockk()

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の playStartSound が呼ばれる`() =
        runTest {
            coEvery { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag) } just Runs
            val useCase = PlayStartSoundForKeyUseCase(engine)

            useCase(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)

            coVerify(exactly = 1) { engine.playStartSound(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag) }
            confirmVerified(engine)
        }
}
