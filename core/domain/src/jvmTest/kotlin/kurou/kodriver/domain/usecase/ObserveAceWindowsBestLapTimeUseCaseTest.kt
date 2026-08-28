@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsBestLapTimeUseCaseTest {
    @MockK
    private lateinit var repo: AceWindowsBestLapTimeRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの bestLapTimeStream を返す`() =
        runTest {
            val expected = AceWindowsBestLapTimeData(bestLapTimeMs = 90_123)
            every { repo.bestLapTimeStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsBestLapTimeUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.bestLapTimeStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.bestLapTimeStream() } returns flowOf()
            val useCase = ObserveAceWindowsBestLapTimeUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.bestLapTimeStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = AceWindowsBestLapTimeData(bestLapTimeMs = 95_000)
            val data2 = AceWindowsBestLapTimeData(bestLapTimeMs = 92_500)
            val data3 = AceWindowsBestLapTimeData(bestLapTimeMs = 91_800)
            every { repo.bestLapTimeStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsBestLapTimeUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.bestLapTimeStream() }
            confirmVerified(repo)
        }
}
