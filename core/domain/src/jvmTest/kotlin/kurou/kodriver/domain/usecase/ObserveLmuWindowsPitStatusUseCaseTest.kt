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
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsPitStatusUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsPitStatusRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの pitStatusStream を返す`() =
        runTest {
            val expected =
                LmuWindowsPitStatusData(inPits = true, pitState = LmuWindowsPitState.ENTERING, inGarageStall = false)
            every { repo.pitStatusStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsPitStatusUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.pitStatusStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.pitStatusStream() } returns flowOf()
            val useCase = ObserveLmuWindowsPitStatusUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.pitStatusStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 =
                LmuWindowsPitStatusData(inPits = false, pitState = LmuWindowsPitState.NONE, inGarageStall = true)
            val data2 =
                LmuWindowsPitStatusData(inPits = true, pitState = LmuWindowsPitState.STOPPED, inGarageStall = false)
            every { repo.pitStatusStream() } returns flowOf(data1, data2)
            val useCase = ObserveLmuWindowsPitStatusUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2), results)
            verify(exactly = 1) { repo.pitStatusStream() }
            confirmVerified(repo)
        }
}
