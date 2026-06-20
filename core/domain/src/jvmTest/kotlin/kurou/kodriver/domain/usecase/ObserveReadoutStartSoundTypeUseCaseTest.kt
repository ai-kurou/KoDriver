package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutStartSoundTypeUseCaseTest {

    @Test
    fun `読み上げ開始音種別を監視できる`() = runBlocking {
        val repository = FakeReadoutStartSoundPreferencesRepository(
            initialType = ReadoutStartSoundType.FORMULA_RADIO,
        )
        val useCase = ObserveReadoutStartSoundTypeUseCase(repository)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, useCase().first())
    }
}
