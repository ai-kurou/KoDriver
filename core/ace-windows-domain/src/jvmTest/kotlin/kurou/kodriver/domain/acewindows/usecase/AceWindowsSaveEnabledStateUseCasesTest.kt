package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test

class AceWindowsSaveEnabledStateUseCasesTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `有効状態のSave系UseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<AceWindowsFlagPreferencesRepository>(
            "SaveAceWindowsFlagEnabledStateUseCase",
            save = { saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.RedFlag, false) },
            useCase = { SaveAceWindowsFlagEnabledStateUseCase(it)(AceWindowsReadoutItemKey.Flag.RedFlag, false) },
        )
        assertSaves<AceWindowsTyreTemperaturePreferencesRepository>(
            "SaveAceWindowsTyreTemperatureEnabledStateUseCase",
            save = { saveEnabledState(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true) },
            useCase = {
                SaveAceWindowsTyreTemperatureEnabledStateUseCase(it)(
                    AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                    true,
                )
            },
        )
        assertSaves<AceWindowsVehicleApproachPreferencesRepository>(
            "SaveAceWindowsVehicleApproachEnabledStateUseCase",
            save = { saveEnabledState(AceWindowsReadoutItemKey.VehicleApproach.StartReadout, false) },
            useCase = {
                SaveAceWindowsVehicleApproachEnabledStateUseCase(it)(
                    AceWindowsReadoutItemKey.VehicleApproach.StartReadout,
                    false,
                )
            },
        )
    }

    private inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        crossinline save: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) = runTest {
        val repository = mockk<R>(relaxUnitFun = true)
        try {
            useCase(repository)
            coVerify(exactly = 1) { repository.save() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
