@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveTyreCarcassTemperatureUseCaseTest {

    @Test
    fun `invoke はリポジトリの tyreCarcassTemperatureStream を返す`() = runBlocking {
        val expected = TyreCarcassTemperatureData(
            wheels = mapOf(WheelIndex.FRONT_LEFT to 350.0),
        )
        val repo = FakeTyreCarcassTemperatureRepository(stream = flowOf(expected))
        val useCase = ObserveTyreCarcassTemperatureUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        val repo = FakeTyreCarcassTemperatureRepository(stream = flowOf())
        val useCase = ObserveTyreCarcassTemperatureUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = TyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 330.0))
        val data2 = TyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 340.0))
        val data3 = TyreCarcassTemperatureData(wheels = mapOf(WheelIndex.FRONT_LEFT to 350.0))
        val repo = FakeTyreCarcassTemperatureRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveTyreCarcassTemperatureUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}

private class FakeTyreCarcassTemperatureRepository(
    private val stream: Flow<TyreCarcassTemperatureData> = flowOf(),
) : TyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> = stream
}
