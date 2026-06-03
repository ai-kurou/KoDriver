package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = FakeReadoutPreferencesRepository()
        val saveUseCase = SaveReadoutEnabledStateUseCase(repo)
        val observeUseCase = ObserveReadoutEnabledStatesUseCase(repo)

        saveUseCase("lmu", "車両接近", true)
        assertEquals(mapOf("車両接近" to true), observeUseCase("lmu").first())

        saveUseCase("lmu", "車両接近", false)
        assertEquals(mapOf("車両接近" to false), observeUseCase("lmu").first())
    }
}
