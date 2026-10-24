package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsFuelUseCaseTest {
    private val repository: LmuWindowsRepository = mockk()

    @Test
    fun `容量が0以下なら流さない`() =
        runTest {
            every { repository.telemetryStream() } returns
                flowOf(
                    telemetry(fuel(5.0, 0.0)),
                    telemetry(fuel(5.0, -1.0)),
                )
            val values = buildList { ObserveLmuWindowsFuelUseCase(repository)().collect(::add) }
            assertEquals(emptyList(), values)
            verify(exactly = 1) { repository.telemetryStream() }
            confirmVerified(repository)
        }

    @Test
    fun `燃料割合を整数化して0から100へcoerceする`() =
        runTest {
            every { repository.telemetryStream() } returns
                flowOf(
                    telemetry(fuel(1.0, 4.0)),
                    telemetry(fuel(-1.0, 4.0)),
                    telemetry(fuel(5.0, 4.0)),
                )
            assertEquals(
                listOf(
                    LmuWindowsRemainingFuelData(25),
                    LmuWindowsRemainingFuelData(0),
                    LmuWindowsRemainingFuelData(100),
                ),
                buildList { ObserveLmuWindowsFuelUseCase(repository)().collect(::add) },
            )
            verify(exactly = 1) { repository.telemetryStream() }
            confirmVerified(repository)
        }
}

private fun fuel(
    current: Double,
    capacity: Double,
) = LmuWindowsFuelData(LmuWindowsFuelUnit(current), LmuWindowsFuelUnit(capacity))

private fun telemetry(fuel: LmuWindowsFuelData) = fakeLmuWindowsTelemetryData().copy(fuel = fuel)
