package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
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

class LmuWindowsSaveReadoutTextUseCasesTest {
    private inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        input: String,
        expected: String,
        crossinline save: suspend R.(String) -> Unit,
        crossinline useCase: suspend (R, String) -> Unit,
    ) = runTest {
        val repository = mockk<R>(relaxUnitFun = true)

        try {
            useCase(repository, input)
            coVerify(exactly = 1) { repository.save(expected) }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }

    @Test
    fun `各SaveUseCaseが正規化した文言をRepositoryに保存するその1`() {
        assertSaves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsBlueFlagReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveBlueFlagText(it) },
            useCase = { repository, text -> SaveLmuWindowsBlueFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository>(
            useCaseName = "SaveLmuWindowsBrakeTemperatureReadoutTextUseCase",
            input = "  温度{celsius}℃  ",
            expected = "温度{celsius}℃",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsBrakeTemperatureReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsVehicleClassBrakeWearPreferencesRepository>(
            useCaseName = "SaveLmuWindowsBrakeWearReadoutTextUseCase",
            input = "  残量{percent}%  ",
            expected = "残量{percent}%",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsBrakeWearReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase",
            input = "  フルコースイエロー  ",
            expected = "フルコースイエロー",
            save = { saveFullCourseYellowFlagText(it) },
            useCase = { repository, text -> SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsMyBestLapPreferencesRepository>(
            useCaseName = "SaveLmuWindowsMyBestLapReadoutTextUseCase",
            input = "  更新{laptime}  ",
            expected = "更新{laptime}",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsMyBestLapReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveFuelImminentReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingFuelReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveFuelReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsPitTimingFuelReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveTyreWearImminentReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(repository)(text)
            },
        )
    }

    @Test
    fun `各SaveUseCaseが正規化した文言をRepositoryに保存するその2`() {
        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveTyreWearReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveVirtualEnergyImminentReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase(repository)(text)
            },
        )

        assertSaves<LmuWindowsPitTimingReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase",
            input = "  ブルー、道を譲れ  ",
            expected = "ブルー、道を譲れ",
            save = { saveVirtualEnergyReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsRedFlagReadoutTextUseCase",
            input = "  レッドフラッグ、セッション中断  ",
            expected = "レッドフラッグ、セッション中断",
            save = { saveRedFlagText(it) },
            useCase = { repository, text -> SaveLmuWindowsRedFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsRemainingFuelPreferencesRepository>(
            useCaseName = "SaveLmuWindowsRemainingFuelReadoutTextUseCase",
            input = "  燃料{percent}%  ",
            expected = "燃料{percent}%",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsRemainingVirtualEnergyPreferencesRepository>(
            useCaseName = "SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase",
            input = "  残量{percent}%  ",
            expected = "残量{percent}%",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsSectorYellowFlagReadoutTextUseCase",
            input = "  イエロー、注意  ",
            expected = "イエロー、注意",
            save = { saveSectorYellowFlagText(it) },
            useCase = { repository, text -> SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase",
            input = "  タイヤが冷えています  ",
            expected = "タイヤが冷えています",
            save = { saveColdReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase(repository)(text) },
        )
    }

    @Test
    fun `各SaveUseCaseが正規化した文言をRepositoryに保存するその3`() {
        assertSaves<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase",
            input = "  タイヤが過熱しています  ",
            expected = "タイヤが過熱しています",
            save = { saveOverheatReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsTyreWearPreferencesRepository>(
            useCaseName = "SaveLmuWindowsTyreWearReadoutTextUseCase",
            input = "  残量{percent}%  ",
            expected = "残量{percent}%",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsTyreWearReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase",
            input = "  左注意  ",
            expected = "左注意",
            save = { saveStartLeftReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(repository)(text)
            },
        )

        assertSaves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase",
            input = "  右注意  ",
            expected = "右注意",
            save = { saveStartRightReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(repository)(text)
            },
        )

        assertSaves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase",
            input = "  左注意  ",
            expected = "左注意",
            save = { saveSustainedLeftReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(repository)(text)
            },
        )

        assertSaves<LmuWindowsVehicleApproachReadoutTextPreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase",
            input = "  右注意  ",
            expected = "右注意",
            save = { saveSustainedRightReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(repository)(text)
            },
        )

        assertSaves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase",
            input = "  オーバーヒート  ",
            expected = "オーバーヒート",
            save = { saveOverheatReadoutText(it) },
            useCase = { repository, text -> SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(repository)(text) },
        )

        assertSaves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase",
            input = "  部品脱落  ",
            expected = "部品脱落",
            save = { savePartDetachedReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)(text)
            },
        )
    }

    @Test
    fun `各SaveUseCaseが正規化した文言をRepositoryに保存するその4`() {
        assertSaves<LmuWindowsVehicleDamagePreferencesRepository>(
            useCaseName = "SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase",
            input = "  タイヤ脱落  ",
            expected = "タイヤ脱落",
            save = { saveTyreDetachedReadoutText(it) },
            useCase = {
                repository,
                text,
                ->
                SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(repository)(text)
            },
        )
    }
}
