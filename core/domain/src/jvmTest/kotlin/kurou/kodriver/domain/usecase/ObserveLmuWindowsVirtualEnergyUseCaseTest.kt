@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsVirtualEnergyUseCaseTest {

    @Test
    fun `invoke はリポジトリの virtualEnergyStream を返す`() = runBlocking {
        val expected = LmuWindowsVirtualEnergyData(remainingRatio = 0.5)
        val repo = mockk<LmuWindowsVirtualEnergyRepository>()
        every { repo.virtualEnergyStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        val repo = mockk<LmuWindowsVirtualEnergyRepository>()
        every { repo.virtualEnergyStream() } returns flowOf()
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = LmuWindowsVirtualEnergyData(remainingRatio = 0.8)
        val data2 = LmuWindowsVirtualEnergyData(remainingRatio = 0.5)
        val data3 = LmuWindowsVirtualEnergyData(remainingRatio = 0.2)
        val repo = mockk<LmuWindowsVirtualEnergyRepository>()
        every { repo.virtualEnergyStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsVirtualEnergyUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
