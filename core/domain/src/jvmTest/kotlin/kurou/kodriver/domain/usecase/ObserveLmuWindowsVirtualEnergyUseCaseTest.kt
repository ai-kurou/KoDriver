@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVirtualEnergyUseCaseTest {

    @MockK
    private lateinit var repo: LmuWindowsVirtualEnergyRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの virtualEnergyStream を返す`() = runBlocking {
        val expected = LmuWindowsVirtualEnergyData(remainingRatio = 0.5)
        every { repo.virtualEnergyStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.virtualEnergyStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        every { repo.virtualEnergyStream() } returns flowOf()
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.virtualEnergyStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = LmuWindowsVirtualEnergyData(remainingRatio = 0.8)
        val data2 = LmuWindowsVirtualEnergyData(remainingRatio = 0.5)
        val data3 = LmuWindowsVirtualEnergyData(remainingRatio = 0.2)
        every { repo.virtualEnergyStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.virtualEnergyStream() }
        confirmVerified(repo)
    }
}
