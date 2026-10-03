package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingEnabledStateUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsPitTimingEnabledStateUseCase(repository)

            useCase(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)
            useCase(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, true)

            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)
            }
            coVerify(exactly = 1) {
                repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, true)
            }
            confirmVerified(repository)
        }
}
