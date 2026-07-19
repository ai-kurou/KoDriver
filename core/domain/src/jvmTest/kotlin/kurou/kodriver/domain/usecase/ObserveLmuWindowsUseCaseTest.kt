package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsUseCaseTest {

    @MockK
    private lateinit var repo: LmuWindowsRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのtelemetryStreamを返す`() = runBlocking {
        val expected = fakeLmuWindowsTelemetryData(speedX = 10.0)
        every { repo.telemetryStream() } returns flowOf(expected)
        val useCase = ObserveLmuWindowsUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.telemetryStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invokeは空のフローをそのまま返す`() = runBlocking {
        every { repo.telemetryStream() } returns flowOf()
        val useCase = ObserveLmuWindowsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.telemetryStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = fakeLmuWindowsTelemetryData(speedX = 1.0)
        val data2 = fakeLmuWindowsTelemetryData(speedX = 2.0)
        val data3 = fakeLmuWindowsTelemetryData(speedX = 3.0)
        every { repo.telemetryStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveLmuWindowsUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.telemetryStream() }
        confirmVerified(repo)
    }
}
