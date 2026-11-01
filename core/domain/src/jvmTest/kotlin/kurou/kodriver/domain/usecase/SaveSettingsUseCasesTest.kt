package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
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
            assertSaves<OverlayTextSizePreferencesRepository>(
                useCaseName = "SaveOverlayTextSizeUseCase",
                save = { saveOverlayTextSize(OverlayTextSize.SMALL) },
                useCase = { SaveOverlayTextSizeUseCase(it)(OverlayTextSize.SMALL) },
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
            assertSaves<OverlayTextColorPreferencesRepository>(
                useCaseName = "SaveOverlayTextColorUseCase",
                save = { saveOverlayTextColor(0xFFFFFFFFL) },
                useCase = { SaveOverlayTextColorUseCase(it)(0xFFFFFFFFL) },
            )
            assertSaves<LowVolumeWarningEnabledPreferencesRepository>(
                useCaseName = "SaveLowVolumeWarningEnabledUseCase",
                save = { saveLowVolumeWarningEnabled(false) },
                useCase = { SaveLowVolumeWarningEnabledUseCase(it)(false) },
            )
            assertSaves<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository>(
                useCaseName = "SaveManualReadoutPlayButtonLayoutEnabledUseCase",
                save = { saveManualReadoutPlayButtonLayoutEnabled(true) },
                useCase = { SaveManualReadoutPlayButtonLayoutEnabledUseCase(it)(true) },
            )
            assertSaves<OverlayVisiblePreferencesRepository>(
                useCaseName = "SaveOverlayVisibleUseCase",
                save = { saveOverlayVisible(true) },
                useCase = { SaveOverlayVisibleUseCase(it)(true) },
            )
            assertSaves<DebugStateCardOrderPreferencesRepository>(
                useCaseName = "SaveDebugStateCardOrderUseCase",
                save = { saveCardOrder(listOf("SESSION", "SIMULATOR")) },
                useCase = { SaveDebugStateCardOrderUseCase(it)(listOf("SESSION", "SIMULATOR")) },
            )
            assertSaves<OverlayWindowBoundsPreferencesRepository>(
                useCaseName = "SaveOverlayWindowBoundsUseCase",
                save = { saveOverlayWindowBounds(OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100)) },
                useCase = {
                    SaveOverlayWindowBoundsUseCase(
                        it,
                    )(OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100))
                },
            )
            assertSaves<VoicePreferencesRepository>(
                useCaseName = "SaveVoiceUseCase",
                save = { saveVoiceId("ja-jp-x-jab-local") },
                useCase = { SaveVoiceUseCase(it)("ja-jp-x-jab-local") },
            )
            assertSaves<ManualReadoutSetPreferencesRepository>(
                useCaseName = "SaveManualReadoutSetsUseCase",
                save = {
                    saveSets(
                        listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
                    )
                },
                useCase = {
                    SaveManualReadoutSetsUseCase(
                        it,
                    )(listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")))
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
