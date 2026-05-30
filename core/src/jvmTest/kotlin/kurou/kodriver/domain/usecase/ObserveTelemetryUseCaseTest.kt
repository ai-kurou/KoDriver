package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first

class ObserveTelemetryUseCaseTest {

    @Test
    fun `invokeはリポジトリのtelemetryStreamを返す`() = runBlocking {
        val expected = fakeTelemetryData(speedX = 10.0)
        val repo = FakeTelemetryRepository(stream = flowOf(expected))
        val useCase = ObserveTelemetryUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeTelemetryRepository(stream = flowOf())
        val useCase = ObserveTelemetryUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeTelemetryData(speedX = 1.0)
        val data2 = fakeTelemetryData(speedX = 2.0)
        val data3 = fakeTelemetryData(speedX = 3.0)
        val repo = FakeTelemetryRepository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveTelemetryUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
