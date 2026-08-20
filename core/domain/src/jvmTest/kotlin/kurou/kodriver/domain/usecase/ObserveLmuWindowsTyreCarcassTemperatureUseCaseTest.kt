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
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveLmuWindowsTyreCarcassTemperatureUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsTyreCarcassTemperatureRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの tyreCarcassTemperatureStream を返す`() =
        runTest {
            val expected =
                LmuWindowsTyreCarcassTemperatureData(
                    wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(350.0f)),
                )
            every { repo.tyreCarcassTemperatureStream() } returns flowOf(expected)
            val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.tyreCarcassTemperatureStream() } returns flowOf()
            val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 =
                LmuWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(330.0f)),
                )
            val data2 =
                LmuWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(340.0f)),
                )
            val data3 =
                LmuWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(350.0f)),
                )
            every { repo.tyreCarcassTemperatureStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveLmuWindowsTyreCarcassTemperatureUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }
}
