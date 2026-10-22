package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperaturePreferencesRepositoryFactoryTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

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
    fun `既存ファイルの全設定と文言を読み出して更新しても互いに保持する`() =
        runTest {
            val key = LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning
            val original =
                LmuWindowsTyreTemperaturePreferences(
                    enabledStates = mapOf(key.value to false),
                    lowWarningPhases = mapOf(SessionPhase.FORMATION.rawValue to true),
                    overheatReadoutText = "過熱の保存文言",
                    coldReadoutText = "",
                )
            tempDir.resolve("lmu_windows_auto_tyre_temperature_preferences.pb").outputStream().use {
                LmuWindowsTyreTemperaturePreferencesSerializer.writeTo(original, it)
            }
            val repositories = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)
            val settings = repositories.preferences
            val texts = repositories.readoutText
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), settings.observeEnabledStates().first())
            assertEquals(mapOf(SessionPhase.FORMATION to true), settings.observeLowWarningPhases().first())
            assertEquals("過熱の保存文言", texts.observeOverheatReadoutText().first())
            assertEquals("", texts.observeColdReadoutText().first())
            texts.saveColdReadoutText("低温の保存文言")
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), settings.observeEnabledStates().first())
            assertEquals(mapOf(SessionPhase.FORMATION to true), settings.observeLowWarningPhases().first())
            assertEquals("過熱の保存文言", texts.observeOverheatReadoutText().first())
            assertEquals("低温の保存文言", texts.observeColdReadoutText().first())
        }
}
