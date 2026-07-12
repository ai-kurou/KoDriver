package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createReadoutStartSoundPreferencesRepository(
    initial: ReadoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
): ReadoutStartSoundPreferencesRepository {
    val repository = mockk<ReadoutStartSoundPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeType() } returns state
    coEvery { repository.saveType(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveReadoutStartSoundTypeUseCaseTest {

    @Test
    fun `読み上げ開始音種別を保存できる`() = runBlocking {
        val repository = createReadoutStartSoundPreferencesRepository()
        val saveUseCase = SaveReadoutStartSoundTypeUseCase(repository)
        val observeUseCase = ObserveReadoutStartSoundTypeUseCase(repository)

        saveUseCase(ReadoutStartSoundType.FORMULA_RADIO)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, observeUseCase().first())
    }
}
