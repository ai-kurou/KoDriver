package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsClassCarGapRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsClassCarGapUseCaseTest {
    private val repo: LmuWindowsClassCarGapRepository = mockk()

    @Test
    fun `invoke はリポジトリの classCarGapStream を返す`() =
        runTest {
            val expected = LmuWindowsClassCarGapData(0.5, 0.5)
            every { repo.classCarGapStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsClassCarGapUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.classCarGapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.classCarGapStream() } returns flowOf()
            val useCase = ObserveLmuWindowsClassCarGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.classCarGapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsClassCarGapData(0.8, 0.8)
            val data2 = LmuWindowsClassCarGapData(0.5, 0.5)
            val data3 = LmuWindowsClassCarGapData(0.2, 0.2)
            every { repo.classCarGapStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsClassCarGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.classCarGapStream() }
            confirmVerified(repo)
        }
}
