package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveSelectedSimulatorUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映される`() = runBlocking {
        val repo = FakeSimulatorPreferencesRepository()
        val saveUseCase = SaveSelectedSimulatorUseCase(repo)
        val observeUseCase = ObserveSelectedSimulatorUseCase(repo)

        saveUseCase("lmu_windows")

        assertEquals("lmu_windows", observeUseCase().first())
    }
}
