package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveDebugStateCardOrderUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: DebugStateCardOrderPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存すると上書きで更新される`() =
        runTest {
            val useCase = SaveDebugStateCardOrderUseCase(repository)
            val firstOrder = listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR)
            val secondOrder = listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.SESSION)

            useCase(firstOrder)
            useCase(secondOrder)

            coVerify(exactly = 1) { repository.saveCardOrder(firstOrder) }
            coVerify(exactly = 1) { repository.saveCardOrder(secondOrder) }
            confirmVerified(repository)
        }
}
