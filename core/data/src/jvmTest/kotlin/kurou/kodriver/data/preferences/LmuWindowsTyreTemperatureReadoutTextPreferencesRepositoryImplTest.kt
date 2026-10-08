package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreTemperatureReadoutTextPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_temperature_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val settings = LmuWindowsTyreTemperaturePreferencesRepositoryImpl(dataStore)
    private val repository = LmuWindowsTyreTemperatureReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `過熱警告文言の既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                repository.observeOverheatReadoutText().first(),
            )
            repository.saveColdReadoutText("タイヤ低温注意")
            settings.saveHighThresholdCelsius(Celsius(100))
            settings.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
            settings.saveLowWarningPhases(setOf(SessionPhase.FORMATION))
            val enabledStates = settings.observeEnabledStates().first()
            val lowWarningPhases = settings.observeLowWarningPhases().first()
            listOf("タイヤが過熱しています", "", " ", "タイヤ過熱注意").forEach { text ->
                repository.saveOverheatReadoutText(text)
                assertEquals(text, repository.observeOverheatReadoutText().first())
                assertEquals("タイヤ低温注意", repository.observeColdReadoutText().first())
                assertEquals(Celsius(100), settings.observeHighThresholdCelsius().first())
                assertEquals(enabledStates, settings.observeEnabledStates().first())
                assertEquals(lowWarningPhases, settings.observeLowWarningPhases().first())
            }
            settings.saveHighThresholdCelsius(Celsius(110))
            settings.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)
            settings.saveLowWarningPhases(emptySet())
            assertEquals("タイヤ過熱注意", repository.observeOverheatReadoutText().first())
        }

    @Test
    fun `低温警告文言の既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
                repository.observeColdReadoutText().first(),
            )
            repository.saveOverheatReadoutText("タイヤ過熱注意")
            settings.saveHighThresholdCelsius(Celsius(100))
            settings.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, false)
            settings.saveLowWarningPhases(setOf(SessionPhase.FORMATION))
            val enabledStates = settings.observeEnabledStates().first()
            val lowWarningPhases = settings.observeLowWarningPhases().first()
            listOf("タイヤが冷えています", "", " ", "タイヤ低温注意").forEach { text ->
                repository.saveColdReadoutText(text)
                assertEquals(text, repository.observeColdReadoutText().first())
                assertEquals("タイヤ過熱注意", repository.observeOverheatReadoutText().first())
                assertEquals(Celsius(100), settings.observeHighThresholdCelsius().first())
                assertEquals(enabledStates, settings.observeEnabledStates().first())
                assertEquals(lowWarningPhases, settings.observeLowWarningPhases().first())
            }
            settings.saveHighThresholdCelsius(Celsius(110))
            settings.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, true)
            settings.saveLowWarningPhases(emptySet())
            assertEquals("タイヤ低温注意", repository.observeColdReadoutText().first())
        }
}
