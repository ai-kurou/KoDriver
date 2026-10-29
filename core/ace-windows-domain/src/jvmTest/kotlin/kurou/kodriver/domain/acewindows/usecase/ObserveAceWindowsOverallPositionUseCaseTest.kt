package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.acewindows.repository.AceWindowsOverallPositionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsOverallPositionUseCaseTest {
    private val repo: AceWindowsOverallPositionRepository = mockk()

    @Test
    fun `invoke はリポジトリの overallPositionStream を返す`() =
        runTest {
            val expected = AceWindowsOverallPositionData(position = 2, totalDrivers = 20)
            every { repo.overallPositionStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsOverallPositionUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.overallPositionStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.overallPositionStream() } returns flowOf()
            val useCase = ObserveAceWindowsOverallPositionUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.overallPositionStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = AceWindowsOverallPositionData(position = 3, totalDrivers = 20)
            val data2 = AceWindowsOverallPositionData(position = 1, totalDrivers = 18)
            val data3 = AceWindowsOverallPositionData(position = 0, totalDrivers = 0)
            every { repo.overallPositionStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsOverallPositionUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.overallPositionStream() }
            confirmVerified(repo)
        }
}
