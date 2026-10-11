package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_tyre_temperature_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は highThresholdCelsius が 95`() =
        runTest {
            val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath).preferences

            assertEquals(
                LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した highThresholdCelsius を読み出せる`() =
        runTest {
            val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath).preferences

            repository.saveHighThresholdCelsius(Celsius(105))

            assertEquals(Celsius(105), repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `既存ファイルの全設定と文言を読み出して更新しても互いに保持する`() =
        runTest {
            val key = LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning
            val original =
                LmuWindowsTyreTemperaturePreferences(
                    highThresholdCelsius = 110,
                    enabledStates = mapOf(key.value to false),
                    lowWarningPhases = mapOf(SessionPhase.FORMATION.rawValue to true),
                    overheatReadoutText = "過熱の保存文言",
                    coldReadoutText = "",
                )
            tempDir.resolve("lmu_windows_tyre_temperature_preferences.pb").outputStream().use {
                LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, it)
            }
            val repositories = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)
            val settings = repositories.preferences
            val texts = repositories.readoutText
            assertEquals(Celsius(110), settings.observeHighThresholdCelsius().first())
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), settings.observeEnabledStates().first())
            assertEquals(mapOf(SessionPhase.FORMATION to true), settings.observeLowWarningPhases().first())
            assertEquals("過熱の保存文言", texts.observeOverheatReadoutText().first())
            assertEquals("", texts.observeColdReadoutText().first())
            texts.saveColdReadoutText("低温の保存文言")
            settings.saveHighThresholdCelsius(Celsius(100))
            assertEquals(Celsius(100), settings.observeHighThresholdCelsius().first())
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), settings.observeEnabledStates().first())
            assertEquals(mapOf(SessionPhase.FORMATION to true), settings.observeLowWarningPhases().first())
            assertEquals("過熱の保存文言", texts.observeOverheatReadoutText().first())
            assertEquals("低温の保存文言", texts.observeColdReadoutText().first())
        }
}
