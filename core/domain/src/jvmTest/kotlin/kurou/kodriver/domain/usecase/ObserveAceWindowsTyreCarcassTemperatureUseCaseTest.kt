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
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAceWindowsTyreCarcassTemperatureUseCaseTest {
    @MockK
    private lateinit var repo: AceWindowsTyreCarcassTemperatureRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke はリポジトリの tyreCarcassTemperatureStream を返す`() =
        runTest {
            val expected =
                AceWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(80.0f)),
                )
            every { repo.tyreCarcassTemperatureStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsTyreCarcassTemperatureUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }

    @Test
    fun `invoke は空のフローをそのまま返す`() =
        runTest {
            every { repo.tyreCarcassTemperatureStream() } returns flowOf()
            val useCase = ObserveAceWindowsTyreCarcassTemperatureUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertTrue(results.isEmpty())
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 =
                AceWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(80.0f)),
                )
            val data2 =
                AceWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(82.0f)),
                )
            val data3 =
                AceWindowsTyreCarcassTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(85.0f)),
                )
            every { repo.tyreCarcassTemperatureStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsTyreCarcassTemperatureUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.tyreCarcassTemperatureStream() }
            confirmVerified(repo)
        }
}
