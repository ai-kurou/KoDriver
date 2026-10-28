package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsGapUseCaseTest {
    private val repo: AceWindowsGapRepository = mockk()

    @Test
    fun `invoke はリポジトリの gapStream を返す`() =
        runTest {
            val expected = AceWindowsGapData(gapAheadSeconds = 1.25f, gapBehindSeconds = -2.5f)
            every { repo.gapStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsGapUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.gapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.gapStream() } returns flowOf()
            val useCase = ObserveAceWindowsGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.gapStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = AceWindowsGapData(gapAheadSeconds = 1.5f, gapBehindSeconds = 2.5f)
            val data2 = AceWindowsGapData(gapAheadSeconds = -1.25f, gapBehindSeconds = -3.5f)
            val data3 = AceWindowsGapData(gapAheadSeconds = 0f, gapBehindSeconds = 0f)
            every { repo.gapStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsGapUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.gapStream() }
            confirmVerified(repo)
        }
}
