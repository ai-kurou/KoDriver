package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsBrakeWearUseCaseTest {
    private val repo: LmuWindowsBrakeWearRepository = mockk()

    @Test
    fun `invoke はリポジトリの brakeWearStream を返す`() =
        runTest {
            val expected =
                LmuWindowsBrakeWearData(
                    wheels = mapOf(WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.02f)),
                )
            every { repo.brakeWearStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsBrakeWearUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.brakeWearStream() } returns flowOf()
            val useCase = ObserveLmuWindowsBrakeWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }

    @Test
    fun `nullを含む複数のデータを順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsBrakeWearData(wheels = mapOf(WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.03f)))
            val data2: LmuWindowsBrakeWearData? = null
            val data3 = LmuWindowsBrakeWearData(wheels = mapOf(WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.03f)))
            every { repo.brakeWearStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsBrakeWearUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.brakeWearStream() }
            confirmVerified(repo)
        }
}
