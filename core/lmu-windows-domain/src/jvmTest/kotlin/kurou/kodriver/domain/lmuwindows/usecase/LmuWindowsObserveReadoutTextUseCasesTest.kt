package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsObserveReadoutTextUseCasesTest {
    private inline fun <reified R : Any> assertObserves(
        useCaseName: String,
        expected: String,
        crossinline observe: R.() -> Flow<String>,
        crossinline useCase: (R) -> Flow<String>,
    ) = runTest {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)

        try {
            assertEquals(expected, useCase(repository).first())
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその1`() {
        assertObserves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsBlueFlagReadoutTextUseCase",
            expected = "ブルー、道を譲れ",
            observe = { observeBlueFlagText() },
            useCase = { ObserveLmuWindowsBlueFlagReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository>(
            useCaseName = "ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase",
            expected = "温度{celsius}℃",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleClassBrakeWearPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsBrakeWearReadoutTextUseCase",
            expected = "残量{percent}%",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsBrakeWearReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase",
            expected = "フルコースイエロー",
            observe = { observeFullCourseYellowFlagText() },
            useCase = { ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsMyBestLapPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsMyBestLapReadoutTextUseCase",
            expected = "更新{laptime}",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsMyBestLapReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeFuelImminentReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingFuelReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeFuelReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingFuelReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeTyreWearImminentReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(it)() },
        )
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその2`() {
        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeTyreWearReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeVirtualEnergyImminentReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase",
            expected = "ピットイン",
            observe = { observeVirtualEnergyReadoutText() },
            useCase = { ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsRedFlagReadoutTextUseCase",
            expected = "レッドフラッグ、セッション中断",
            observe = { observeRedFlagText() },
            useCase = { ObserveLmuWindowsRedFlagReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsRemainingFuelPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsRemainingFuelReadoutTextUseCase",
            expected = "残量{percent}%",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsRemainingFuelReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsRemainingVirtualEnergyPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase",
            expected = "残量{percent}%",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase",
            expected = "イエロー、注意",
            observe = { observeSectorYellowFlagText() },
            useCase = { ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase",
            expected = "タイヤが冷えています",
            observe = { observeColdReadoutText() },
            useCase = { ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase(it)() },
        )
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその3`() {
        assertObserves<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase",
            expected = "タイヤが過熱しています",
            observe = { observeOverheatReadoutText() },
            useCase = { ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsTyreWearPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsTyreWearReadoutTextUseCase",
            expected = "残量{percent}%",
            observe = { observeReadoutText() },
            useCase = { ObserveLmuWindowsTyreWearReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase",
            expected = "左注意",
            observe = { observeStartLeftReadoutText() },
            useCase = { ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase",
            expected = "右注意",
            observe = { observeStartRightReadoutText() },
            useCase = { ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase",
            expected = "左注意",
            observe = { observeSustainedLeftReadoutText() },
            useCase = { ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase",
            expected = "右注意",
            observe = { observeSustainedRightReadoutText() },
            useCase = { ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase",
            expected = "オーバーヒート",
            observe = { observeOverheatReadoutText() },
            useCase = { ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(it)() },
        )

        assertObserves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase",
            expected = "部品脱落",
            observe = { observePartDetachedReadoutText() },
            useCase = { ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(it)() },
        )
    }

    @Test
    fun `各ObserveUseCaseがRepositoryの値をそのまま流すその4`() {
        assertObserves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase",
            expected = "タイヤ脱落",
            observe = { observeTyreDetachedReadoutText() },
            useCase = { ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(it)() },
        )
    }
}
