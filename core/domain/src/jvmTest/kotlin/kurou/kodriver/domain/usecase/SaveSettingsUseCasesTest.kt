package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test

class SaveSettingsUseCasesTest {
    @Test
    fun `設定保存UseCaseはRepositoryの対応するメソッドへ委譲する その1`() =
        runTest {
            assertSaves<ConsoleAddressPreferencesRepository>(
                useCaseName = "SaveConsoleAddressUseCase",
                save = { saveConsoleAddress("192.168.1.50") },
                useCase = { SaveConsoleAddressUseCase(it)("192.168.1.50") },
            )
            assertSaves<DynamicColorEnabledRepository>(
                useCaseName = "SaveDynamicColorEnabledUseCase",
                save = { saveDynamicColorEnabled(true) },
                useCase = { SaveDynamicColorEnabledUseCase(it)(true) },
            )
            assertSaves<KeepScreenOnEnabledRepository>(
                useCaseName = "SaveKeepScreenOnEnabledUseCase",
                save = { saveKeepScreenOn(true) },
                useCase = { SaveKeepScreenOnEnabledUseCase(it)(true) },
            )
            assertSaves<AndroidAudioUsagePreferencesRepository>(
                useCaseName = "SaveAndroidAudioUsageUseCase",
                save = { saveAndroidAudioUsage(AndroidAudioUsage.ASSISTANT) },
                useCase = { SaveAndroidAudioUsageUseCase(it)(AndroidAudioUsage.ASSISTANT) },
            )
            assertSaves<ReadoutStartSoundPreferencesRepository>(
                useCaseName = "SaveReadoutStartSoundTypeUseCase",
                save = { saveType(ReadoutStartSoundType.FORMULA_RADIO) },
                useCase = { SaveReadoutStartSoundTypeUseCase(it)(ReadoutStartSoundType.FORMULA_RADIO) },
            )
            assertSaves<SimulatorPreferencesRepository>(
                useCaseName = "SaveSelectedSimulatorUseCase",
                save = { saveSelectedSimulator(Simulator.LmuWindows) },
                useCase = { SaveSelectedSimulatorUseCase(it)(Simulator.LmuWindows) },
            )
            assertSaves<ThemePreferencesRepository>(
                useCaseName = "SaveThemeModeUseCase",
                save = { saveThemeMode(ThemeMode.LIGHT) },
                useCase = { SaveThemeModeUseCase(it)(ThemeMode.LIGHT) },
            )
        }

    @Test
    fun `設定保存UseCaseはRepositoryの対応するメソッドへ委譲する その2`() =
        runTest {
            assertSaves<LowVolumeWarningEnabledPreferencesRepository>(
                useCaseName = "SaveLowVolumeWarningEnabledUseCase",
                save = { saveLowVolumeWarningEnabled(false) },
                useCase = { SaveLowVolumeWarningEnabledUseCase(it)(false) },
            )
            assertSaves<DebugStateCardOrderPreferencesRepository>(
                useCaseName = "SaveDebugStateCardOrderUseCase",
                save = { saveCardOrder(listOf("SESSION", "SIMULATOR")) },
                useCase = { SaveDebugStateCardOrderUseCase(it)(listOf("SESSION", "SIMULATOR")) },
            )
            assertSaves<MainWindowBoundsPreferencesRepository>(
                useCaseName = "SaveMainWindowBoundsUseCase",
                save = { saveMainWindowBounds(MainWindowBounds(x = 10, y = 20, width = 800, height = 500)) },
                useCase = {
                    SaveMainWindowBoundsUseCase(
                        it,
                    )(MainWindowBounds(x = 10, y = 20, width = 800, height = 500))
                },
            )
        }

    private suspend inline fun <reified R : Any> assertSaves(
        useCaseName: String,
        crossinline save: suspend R.() -> Unit,
        crossinline useCase: suspend (R) -> Unit,
    ) {
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
