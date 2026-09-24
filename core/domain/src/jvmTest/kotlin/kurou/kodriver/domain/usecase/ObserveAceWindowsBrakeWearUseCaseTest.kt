@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsBrakeWearRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsBrakeWearUseCaseTest {
    private val repo: AceWindowsBrakeWearRepository = mockk()

    @Test
    fun `invoke はリポジトリの brakeWearStream を返す`() =
        runTest {
            val expected =
                AceWindowsBrakeWearData(
                    padLife = mapOf(WheelIndex.FRONT_LEFT to 0.9),
                    discLife = mapOf(WheelIndex.FRONT_LEFT to 0.8),
                )
            every { repo.brakeWearStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsBrakeWearUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.brakeWearStream() } returns flowOf()
            val useCase = ObserveAceWindowsBrakeWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 =
                AceWindowsBrakeWearData(
                    padLife = mapOf(WheelIndex.FRONT_LEFT to 0.9),
                    discLife = mapOf(WheelIndex.FRONT_LEFT to 0.8),
                )
            val data2 =
                AceWindowsBrakeWearData(
                    padLife = mapOf(WheelIndex.FRONT_LEFT to 0.85),
                    discLife = mapOf(WheelIndex.FRONT_LEFT to 0.75),
                )
            every { repo.brakeWearStream() } returns flowOf(data1, data2)
            val useCase = ObserveAceWindowsBrakeWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2), results)
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }
}
