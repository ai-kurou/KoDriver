@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsRemainingFuelLapsUseCaseTest {
    private val repo: AceWindowsRemainingFuelLapsRepository = mockk()

    @Test
    fun `invoke はリポジトリの remainingFuelLapsStream を返す`() =
        runTest {
            val expected = AceWindowsRemainingFuelLapsData(remainingLaps = 12.5f)
            every { repo.remainingFuelLapsStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsRemainingFuelLapsUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.remainingFuelLapsStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.remainingFuelLapsStream() } returns flowOf()
            val useCase = ObserveAceWindowsRemainingFuelLapsUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.remainingFuelLapsStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = AceWindowsRemainingFuelLapsData(remainingLaps = 3.9f)
            val data2 = AceWindowsRemainingFuelLapsData(remainingLaps = 3.2f)
            val data3 = AceWindowsRemainingFuelLapsData(remainingLaps = 2.6f)
            every { repo.remainingFuelLapsStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsRemainingFuelLapsUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.remainingFuelLapsStream() }
            confirmVerified(repo)
        }
}
