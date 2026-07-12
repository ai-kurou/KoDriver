@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsTyreCarcassTemperatureUseCaseTest {

    @Test
    fun `invoke はリポジトリの tyreCarcassTemperatureStream を返す`() = runBlocking {
        val expected = LmuWindowsTyreCarcassTemperatureData(
            wheels = mapOf(WheelIndex.FRONT_LEFT to 350.0),
        )
        val repo = mockk<LmuWindowsTyreCarcassTemperatureRepository>()
        every { repo.tyreCarcassTemperatureStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        val repo = mockk<LmuWindowsTyreCarcassTemperatureRepository>()
        every { repo.tyreCarcassTemperatureStream() } returns flowOf()
        val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = LmuWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 330.0))
        val data2 = LmuWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 340.0))
        val data3 = LmuWindowsTyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 350.0))
        val repo = mockk<LmuWindowsTyreCarcassTemperatureRepository>()
        every { repo.tyreCarcassTemperatureStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
