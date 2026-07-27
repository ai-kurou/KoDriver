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
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsFuelUseCaseTest {

    @MockK
    private lateinit var repo: AceWindowsFuelRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの fuelStream を返す`() = runBlocking {
        val expected = AceWindowsFuelData(remainingPercent = 50.0)
        every { repo.fuelStream() } returns flowOf(expected)
        val useCase = ObserveAceWindowsFuelUseCase(repo)

        val result = useCase().first()

        assertEquals(expected, result)
        verify(exactly = 1) { repo.fuelStream() }
        confirmVerified(repo)
    }

    @Test
    fun `invoke は空のフローをそのまま返す`() = runBlocking {
        every { repo.fuelStream() } returns flowOf()
        val useCase = ObserveAceWindowsFuelUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertTrue(results.isEmpty())
        verify(exactly = 1) { repo.fuelStream() }
        confirmVerified(repo)
    }

    @Test
    fun `複数のデータを順番通りに流す`() = runBlocking {
        val data1 = AceWindowsFuelData(remainingPercent = 80.0)
        val data2 = AceWindowsFuelData(remainingPercent = 50.0)
        val data3 = AceWindowsFuelData(remainingPercent = 20.0)
        every { repo.fuelStream() } returns flowOf(data1, data2, data3)
        val useCase = ObserveAceWindowsFuelUseCase(repo)

        val results = buildList { useCase().collect { add(it) } }

        assertEquals(listOf(data1, data2, data3), results)
        verify(exactly = 1) { repo.fuelStream() }
        confirmVerified(repo)
    }
}
