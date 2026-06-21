package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveGt7Ps5UseCaseTest {

    @Test
    fun `invokeはリポジトリのtelemetryStreamを返す`() = runBlocking {
        val expected = fakeGt7Ps5TelemetryData(lapCount = 3)
        val repo = FakeGt7Ps5Repository(stream = flowOf(expected))
        val useCase = ObserveGt7Ps5UseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        val repo = FakeGt7Ps5Repository(stream = flowOf())
        val useCase = ObserveGt7Ps5UseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeGt7Ps5TelemetryData(lapCount = 1)
        val data2 = fakeGt7Ps5TelemetryData(lapCount = 2)
        val data3 = fakeGt7Ps5TelemetryData(lapCount = 3)
        val repo = FakeGt7Ps5Repository(stream = flowOf(data1, data2, data3))
        val useCase = ObserveGt7Ps5UseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
    }
}
