package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GTE_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleClassBrakeWearPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_vehicle_class_brake_wear_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassBrakeWearPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleClassBrakeWearPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は全クラス分のデフォルト閾値`() =
        runTest {
            val expected =
                lmuWindowsAllVehicleClasses.associateWith {
                    lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(it)
                }

            assertEquals(expected, repository.observeLowThresholdPercent().first())
        }

    @Test
    fun `saveLowThresholdPercent で保存したクラスの値だけが更新され他クラスはデフォルトのまま`() =
        runTest {
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)

            val result = repository.observeLowThresholdPercent().first()

            assertEquals(25, result[LmuWindowsVehicleClassData.Gte])
            assertEquals(
                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.Gt3),
                result[LmuWindowsVehicleClassData.Gt3],
            )
        }

    @Test
    fun `saveLowThresholdPercent を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 15)
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)

            assertEquals(
                25,
                repository.observeLowThresholdPercent().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `Unknownクラスは raw 値によらず1つの閾値を共有する`() =
        runTest {
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Unknown("Formula2026"), 30)

            val result = repository.observeLowThresholdPercent().first()
            val unknownEntry = result.entries.single { it.key is LmuWindowsVehicleClassData.Unknown }

            assertEquals(30, unknownEntry.value)
        }

    @Test
    fun `GTEのデフォルト値定数を用いてデフォルト閾値を検証できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GTE_DEFAULT,
                repository.observeLowThresholdPercent().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `対象クラスの初期選択値はデフォルト値`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT,
                repository.observeSelectedVehicleClass().first(),
            )
        }

    @Test
    fun `saveSelectedVehicleClass で保存したクラスが選択値として反映される`() =
        runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)

            assertEquals(LmuWindowsVehicleClassData.Gte, repository.observeSelectedVehicleClass().first())
        }

    @Test
    fun `saveSelectedVehicleClass を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gt3)

            assertEquals(LmuWindowsVehicleClassData.Gt3, repository.observeSelectedVehicleClass().first())
        }

    @Test
    fun `選択クラスがUnknownの場合は代表キーとして保存・復元される`() =
        runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Unknown("Formula2026"))

            val result = repository.observeSelectedVehicleClass().first()

            assertEquals(true, result is LmuWindowsVehicleClassData.Unknown)
        }

    @Test
    fun `共通文言を保存しても車両クラスの閾値と選択は維持される`() =
        runTest {
            assertEquals(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 15)
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)
            listOf("温度{percent}℃", "", " ", "過熱警告").forEach { text ->
                repository.saveReadoutText(text)
                assertEquals(text, repository.observeReadoutText().first())
                assertEquals(15, repository.observeLowThresholdPercent().first()[LmuWindowsVehicleClassData.Gte])
                assertEquals(LmuWindowsVehicleClassData.Gte, repository.observeSelectedVehicleClass().first())
            }
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gt3)
            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gt3, 40)
            assertEquals("過熱警告", repository.observeReadoutText().first())
        }
}
