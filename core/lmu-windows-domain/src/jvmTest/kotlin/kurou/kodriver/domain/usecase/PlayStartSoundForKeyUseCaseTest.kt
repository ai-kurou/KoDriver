package kurou.kodriver.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test

class PlayStartSoundForKeyUseCaseTest {
    private val engine: TextToSpeechEngine = mockk()

    @Test
    fun `invoke を呼ぶと TextToSpeechEngine の playStartSound が呼ばれる`() =
        runTest {
            coEvery { engine.playStartSound(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag) } just Runs
            val useCase = PlayStartSoundForKeyUseCase(engine)

            useCase(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag)

            coVerify(exactly = 1) { engine.playStartSound(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag) }
            confirmVerified(engine)
        }
}
