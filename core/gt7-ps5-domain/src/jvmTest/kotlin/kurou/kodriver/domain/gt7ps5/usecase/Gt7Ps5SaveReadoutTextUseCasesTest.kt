package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kotlin.test.Test

class Gt7Ps5SaveReadoutTextUseCasesTest {
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
        assertSaves<Gt7Ps5MyBestLapPreferencesRepository>(
            useCaseName = "SaveGt7Ps5MyBestLapReadoutTextUseCase",
            input = "  更新{laptime}  ",
            expected = "更新{laptime}",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveGt7Ps5MyBestLapReadoutTextUseCase(repository)(text) },
        )

        assertSaves<Gt7Ps5RemainingFuelLapsPreferencesRepository>(
            useCaseName = "SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase",
            input = "  燃料なし  ",
            expected = "燃料なし",
            save = { saveEmptyReadoutText(it) },
            useCase = { repository, text -> SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(repository)(text) },
        )

        assertSaves<Gt7Ps5RemainingFuelLapsPreferencesRepository>(
            useCaseName = "SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase",
            input = "  残り{laps}周  ",
            expected = "残り{laps}周",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase(repository)(text) },
        )

        assertSaves<Gt7Ps5RemainingFuelPreferencesRepository>(
            useCaseName = "SaveGt7Ps5RemainingFuelReadoutTextUseCase",
            input = "  残り{percent}パーセント  ",
            expected = "残り{percent}パーセント",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveGt7Ps5RemainingFuelReadoutTextUseCase(repository)(text) },
        )

        assertSaves<Gt7Ps5TyreTemperaturePreferencesRepository>(
            useCaseName = "SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase",
            input = "  タイヤが過熱しています  ",
            expected = "タイヤが過熱しています",
            save = { saveOverheatReadoutText(it) },
            useCase = { repository, text -> SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(repository)(text) },
        )
    }
}
