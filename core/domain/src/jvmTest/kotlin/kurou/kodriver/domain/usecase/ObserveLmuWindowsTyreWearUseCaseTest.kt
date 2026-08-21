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
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsTyreWearUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsTyreWearRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの tyreWearStream を返す`() =
        runTest {
            val expected =
                LmuWindowsTyreWearData(
                    wheels = mapOf(WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(0.8)),
                )
            every { repo.tyreWearStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsTyreWearUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.tyreWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.tyreWearStream() } returns flowOf()
            val useCase = ObserveLmuWindowsTyreWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.tyreWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsTyreWearData(wheels = mapOf(WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(1.0)))
            val data2 = LmuWindowsTyreWearData(wheels = mapOf(WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(0.9)))
            val data3 = LmuWindowsTyreWearData(wheels = mapOf(WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(0.8)))
            every { repo.tyreWearStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsTyreWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.tyreWearStream() }
            confirmVerified(repo)
        }
}
