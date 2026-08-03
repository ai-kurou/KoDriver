package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveGt7Ps5UseCaseTest {
    @MockK
    private lateinit var repo: Gt7Ps5Repository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのtelemetryStreamを返す`() =
        runTest {
            val expected = fakeGt7Ps5TelemetryData(lapCount = 3)
            every { repo.telemetryStream() } returns flowOf(expected)
            val useCase = ObserveGt7Ps5UseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invokeは空のフローをそのまま返す`() =
        runTest {
            every { repo.telemetryStream() } returns flowOf()
            val useCase = ObserveGt7Ps5UseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = fakeGt7Ps5TelemetryData(lapCount = 1)
            val data2 = fakeGt7Ps5TelemetryData(lapCount = 2)
            val data3 = fakeGt7Ps5TelemetryData(lapCount = 3)
            every { repo.telemetryStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveGt7Ps5UseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }
}
