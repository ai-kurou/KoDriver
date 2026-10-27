package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsCarGapUseCaseTest {
    private val repo: LmuWindowsCarGapRepository = mockk()

    @Test
    fun `invoke はリポジトリの carGapStream を返す`() =
        runTest {
            val expected = LmuWindowsCarGapData(0.5, 0.5, 0.5, 0.5)
            every { repo.carGapStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsCarGapUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.carGapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.carGapStream() } returns flowOf()
            val useCase = ObserveLmuWindowsCarGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.carGapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsCarGapData(0.8, 0.8, 0.8, 0.8)
            val data2 = LmuWindowsCarGapData(0.5, 0.5, 0.5, 0.5)
            val data3 = LmuWindowsCarGapData(0.2, 0.2, 0.2, 0.2)
            every { repo.carGapStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsCarGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.carGapStream() }
            confirmVerified(repo)
        }
}
