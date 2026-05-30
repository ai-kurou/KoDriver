package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuUseCaseTest {

    @Test
    fun `invokeはリポジトリのtelemetryStreamを返す`() = runBlocking {
        val expected = fakeLmuTelemetryData(speedX = 10.0)
        val repo = FakeLmuRepository(stream = flowOf(expected))
        val useCase = ObserveLmuUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeLmuRepository(stream = flowOf())
        val useCase = ObserveLmuUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeLmuTelemetryData(speedX = 1.0)
        val data2 = fakeLmuTelemetryData(speedX = 2.0)
        val data3 = fakeLmuTelemetryData(speedX = 3.0)
        val repo = FakeLmuRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveLmuUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
