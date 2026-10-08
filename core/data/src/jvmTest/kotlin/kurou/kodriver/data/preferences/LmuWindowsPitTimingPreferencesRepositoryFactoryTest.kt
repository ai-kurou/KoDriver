package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsPitTimingPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_pit_timing_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は両方とも3周`() =
        runTest {
            val repository =
                createLmuWindowsPitTimingPreferencesRepository(
                    directory = tempDir.absolutePath,
                ).preferences

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                repository.observeVirtualEnergyLaps().first(),
            )
            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, repository.observeTyreWearLaps().first())
        }

    @Test
    fun `保存した予想残り周回数を読み出せる`() =
        runTest {
            val repository =
                createLmuWindowsPitTimingPreferencesRepository(
                    directory = tempDir.absolutePath,
                ).preferences

            repository.saveVirtualEnergyLaps(5)
            repository.saveTyreWearLaps(1)

            assertEquals(5, repository.observeVirtualEnergyLaps().first())
            assertEquals(1, repository.observeTyreWearLaps().first())
        }

    @Test
    fun `共有DataStoreで設定と全四文言を交互に保存しても互いの値を保持する`() =
        runTest {
            val repositories = createLmuWindowsPitTimingPreferencesRepository(tempDir.absolutePath)
            val preferences = repositories.preferences
            val text = repositories.readoutText
            text.saveVirtualEnergyReadoutText("VE予告")
            preferences.saveVirtualEnergyLaps(4)
            text.saveTyreWearReadoutText("タイヤ予告")
            preferences.saveTyreWearLaps(2)
            text.saveVirtualEnergyImminentReadoutText("VE直前")
            preferences.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.TyreWear, false)
            text.saveTyreWearImminentReadoutText("タイヤ直前")

            assertEquals(4, preferences.observeVirtualEnergyLaps().first())
            assertEquals(2, preferences.observeTyreWearLaps().first())
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.PitTiming.TyreWear to false),
                preferences.observeEnabledStates().first(),
            )
            assertEquals("VE予告", text.observeVirtualEnergyReadoutText().first())
            assertEquals("タイヤ予告", text.observeTyreWearReadoutText().first())
            assertEquals("VE直前", text.observeVirtualEnergyImminentReadoutText().first())
            assertEquals("タイヤ直前", text.observeTyreWearImminentReadoutText().first())
        }

    @Test
    fun `既存ファイルの周回数と有効状態と全四文言を分離後も読み出せる`() =
        runTest {
            val original =
                LmuWindowsPitTimingPreferences(
                    virtualEnergyLaps = 5,
                    tyreWearLaps = 1,
                    enabledStates = mapOf(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy.value to false),
                    virtualEnergyReadoutText = "VE予告",
                    virtualEnergyImminentReadoutText = "",
                    tyreWearReadoutText = "タイヤ予告",
                    tyreWearImminentReadoutText = "タイヤ直前",
                )
            tempDir.resolve("lmu_windows_pit_timing_preferences.pb").outputStream().use {
                LmuWindowsPitTimingPreferencesSerializer.writeTo(original, it)
            }
            val repositories = createLmuWindowsPitTimingPreferencesRepository(tempDir.absolutePath)
            assertEquals(5, repositories.preferences.observeVirtualEnergyLaps().first())
            assertEquals(1, repositories.preferences.observeTyreWearLaps().first())
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to false),
                repositories.preferences.observeEnabledStates().first(),
            )
            assertEquals("VE予告", repositories.readoutText.observeVirtualEnergyReadoutText().first())
            assertEquals("", repositories.readoutText.observeVirtualEnergyImminentReadoutText().first())
            assertEquals("タイヤ予告", repositories.readoutText.observeTyreWearReadoutText().first())
            assertEquals("タイヤ直前", repositories.readoutText.observeTyreWearImminentReadoutText().first())
        }
}
