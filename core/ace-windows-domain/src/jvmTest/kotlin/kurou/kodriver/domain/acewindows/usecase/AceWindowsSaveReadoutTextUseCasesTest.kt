package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class AceWindowsSaveReadoutTextUseCasesTest {
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
        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsBlackFlagReadoutTextUseCase",
            input = "  ブラックフラッグ  ",
            expected = "ブラックフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.BLACK, it) },
            useCase = { repository, text -> SaveAceWindowsBlackFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsBlackWhiteFlagReadoutTextUseCase",
            input = "  ブラック・ホワイトフラッグ  ",
            expected = "ブラック・ホワイトフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, it) },
            useCase = { repository, text -> SaveAceWindowsBlackWhiteFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsBlueFlagReadoutTextUseCase",
            input = "  ブルーフラッグ  ",
            expected = "ブルーフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.BLUE, it) },
            useCase = { repository, text -> SaveAceWindowsBlueFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsCheckeredFlagReadoutTextUseCase",
            input = "  チェッカー、完走  ",
            expected = "チェッカー、完走",
            save = { saveText(AceWindowsFlagReadoutTextKey.CHECKERED, it) },
            useCase = { repository, text -> SaveAceWindowsCheckeredFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsGreenFlagReadoutTextUseCase",
            input = "  グリーンフラッグ  ",
            expected = "グリーンフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.GREEN, it) },
            useCase = { repository, text -> SaveAceWindowsGreenFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsMyBestLapPreferencesRepository>(
            useCaseName = "SaveAceWindowsMyBestLapReadoutTextUseCase",
            input = "  更新{laptime}  ",
            expected = "更新{laptime}",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsMyBestLapReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsOrangeCircleFlagReadoutTextUseCase",
            input = "  オレンジボールフラッグ、車両に不具合があります  ",
            expected = "オレンジボールフラッグ、車両に不具合があります",
            save = { saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, it) },
            useCase = { repository, text -> SaveAceWindowsOrangeCircleFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsRedFlagReadoutTextUseCase",
            input = "  レッドフラッグ  ",
            expected = "レッドフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.RED, it) },
            useCase = { repository, text -> SaveAceWindowsRedFlagReadoutTextUseCase(repository)(text) },
        )
    }

    @Test
    fun `各SaveUseCaseが正規化した文言をRepositoryに保存するその2`() {
        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase",
            input = "  レッド・イエローストライプフラッグ、路面が滑りやすいです  ",
            expected = "レッド・イエローストライプフラッグ、路面が滑りやすいです",
            save = { saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, it) },
            useCase = { repository, text -> SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsRemainingFuelLapsPreferencesRepository>(
            useCaseName = "SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase",
            input = "  燃料なし  ",
            expected = "燃料なし",
            save = { saveEmptyReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsRemainingFuelLapsPreferencesRepository>(
            useCaseName = "SaveAceWindowsRemainingFuelLapsReadoutTextUseCase",
            input = "  残り{laps}周  ",
            expected = "残り{laps}周",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsRemainingFuelLapsReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsRemainingFuelPreferencesRepository>(
            useCaseName = "SaveAceWindowsRemainingFuelReadoutTextUseCase",
            input = "  残り{percent}パーセント  ",
            expected = "残り{percent}パーセント",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsTyreTemperaturePreferencesRepository>(
            useCaseName = "SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase",
            input = "  タイヤが過熱しています  ",
            expected = "タイヤが過熱しています",
            save = { saveOverheatReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsVehicleApproachPreferencesRepository>(
            useCaseName = "SaveAceWindowsVehicleApproachReadoutTextUseCase",
            input = "  車両接近  ",
            expected = "車両接近",
            save = { saveReadoutText(it) },
            useCase = { repository, text -> SaveAceWindowsVehicleApproachReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsWhiteFlagReadoutTextUseCase",
            input = "  ホワイトフラッグ  ",
            expected = "ホワイトフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.WHITE, it) },
            useCase = { repository, text -> SaveAceWindowsWhiteFlagReadoutTextUseCase(repository)(text) },
        )

        assertSaves<AceWindowsFlagReadoutTextPreferencesRepository>(
            useCaseName = "SaveAceWindowsYellowFlagReadoutTextUseCase",
            input = "  イエローフラッグ  ",
            expected = "イエローフラッグ",
            save = { saveText(AceWindowsFlagReadoutTextKey.YELLOW, it) },
            useCase = { repository, text -> SaveAceWindowsYellowFlagReadoutTextUseCase(repository)(text) },
        )
    }
}
