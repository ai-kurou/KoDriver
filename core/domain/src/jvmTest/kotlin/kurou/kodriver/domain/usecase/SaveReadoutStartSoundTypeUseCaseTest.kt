package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.Test

class SaveReadoutStartSoundTypeUseCaseTest {

    @Test
    fun `読み上げ開始音種別を保存できる`() = runBlocking {
        val repository = mockk<ReadoutStartSoundPreferencesRepository>(relaxUnitFun = true)

        SaveReadoutStartSoundTypeUseCase(repository)(ReadoutStartSoundType.FORMULA_RADIO)

        coVerify(exactly = 1) { repository.saveType(ReadoutStartSoundType.FORMULA_RADIO) }
        confirmVerified(repository)
    }
}
