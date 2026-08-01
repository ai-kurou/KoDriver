@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsFlagUseCaseTest {

    @MockK
    private lateinit var repo: AceWindowsFlagRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの flagStream を返す`() =
        runBlocking {
        val expected = AceWindowsFlagData(flag = AceWindowsFlagType.BLUE_FLAG)
        every { repo.flagStream() } returns flowOf(expected)
        val useCase = ObserveAceWindowsFlagUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runBlocking {
        every { repo.flagStream() } returns flowOf()
        val useCase = ObserveAceWindowsFlagUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runBlocking {
        val data1 = AceWindowsFlagData(flag = AceWindowsFlagType.GREEN_FLAG)
        val data2 = AceWindowsFlagData(flag = AceWindowsFlagType.YELLOW_FLAG)
        val data3 = AceWindowsFlagData(flag = AceWindowsFlagType.CHECKERED_FLAG)
        every { repo.flagStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveAceWindowsFlagUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.flagStream() }
        confirmVerified(repo)
    }
}
