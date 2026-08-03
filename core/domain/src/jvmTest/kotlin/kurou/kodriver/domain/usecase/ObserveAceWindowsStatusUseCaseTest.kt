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
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsStatusUseCaseTest {
    @MockK
    private lateinit var repo: AceWindowsStatusRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの statusStream を返す`() =
        runTest {
            val expected = AceWindowsStatusData(status = AceWindowsStatusType.LIVE)
            every { repo.statusStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsStatusUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.statusStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.statusStream() } returns flowOf()
            val useCase = ObserveAceWindowsStatusUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.statusStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = AceWindowsStatusData(status = AceWindowsStatusType.OFF)
            val data2 = AceWindowsStatusData(status = AceWindowsStatusType.PAUSE)
            val data3 = AceWindowsStatusData(status = AceWindowsStatusType.LIVE)
            every { repo.statusStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsStatusUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.statusStream() }
            confirmVerified(repo)
        }
}
