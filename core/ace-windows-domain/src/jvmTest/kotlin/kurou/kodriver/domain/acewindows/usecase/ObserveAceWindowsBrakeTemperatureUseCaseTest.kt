package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsBrakeTemperatureData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsBrakeTemperatureUseCaseTest {
    private val repo: AceWindowsBrakeTemperatureRepository = mockk()

    @Test
    fun `invoke はリポジトリの brakeTemperatureStream を返す`() =
        runTest {
            val expected =
                AceWindowsBrakeTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(80.0f)),
                )
            every { repo.brakeTemperatureStream() } returns flowOf(expected)
            val useCase = ObserveAceWindowsBrakeTemperatureUseCase(repo)

            val result = useCase().first()

            assertEquals(expected, result)
            verify(exactly = 1) { repo.brakeTemperatureStream() }
            confirmVerified(repo)
        }

    @Test
    fun `複数のデータを順番通りに流す`() =
        runTest {
            val data1 =
                AceWindowsBrakeTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(80.0f)),
                )
            val data2 =
                AceWindowsBrakeTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(82.0f)),
                )
            val data3 =
                AceWindowsBrakeTemperatureData(
                    wheels =
                        mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(85.0f)),
                )
            every { repo.brakeTemperatureStream() } returns flowOf(data1, data2, data3)
            val useCase = ObserveAceWindowsBrakeTemperatureUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2, data3), results)
            verify(exactly = 1) { repo.brakeTemperatureStream() }
            confirmVerified(repo)
        }
}
