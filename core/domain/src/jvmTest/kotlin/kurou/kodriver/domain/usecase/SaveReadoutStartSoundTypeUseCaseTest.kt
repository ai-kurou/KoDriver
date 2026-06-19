package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutStartSoundTypeUseCaseTest {

    @Test
    fun `読み上げ開始音種別を保存できる`() = runBlocking {
        val repository = FakeReadoutStartSoundRepository()
        val saveUseCase = SaveReadoutStartSoundTypeUseCase(repository)
        val observeUseCase = ObserveReadoutStartSoundTypeUseCase(repository)

        saveUseCase(ReadoutStartSoundType.FORMULA_RADIO)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, observeUseCase().first())
    }
}
