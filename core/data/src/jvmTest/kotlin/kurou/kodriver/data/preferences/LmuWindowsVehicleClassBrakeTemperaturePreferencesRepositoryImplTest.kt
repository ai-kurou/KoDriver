package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleClassBrakeTemperaturePreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_vehicle_class_brake_temperature_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleClassBrakeTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は全クラス分のデフォルト閾値`() =
        runTest {
            val expected =
                lmuWindowsAllVehicleClasses.associateWith {
                    lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(it)
                }

            assertEquals(expected, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `saveHighThresholdCelsius で保存したクラスの値だけが更新され他クラスはデフォルトのまま`() =
        runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)

            val result = repository.observeHighThresholdCelsius().first()

            assertEquals(800, result[LmuWindowsVehicleClassData.Gte])
            assertEquals(
                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gt3),
                result[LmuWindowsVehicleClassData.Gt3],
            )
        }

    @Test
    fun `saveHighThresholdCelsius を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 650)
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 800)

            assertEquals(
                800,
                repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `Unknownクラスは raw 値によらず1つの閾値を共有する`() =
        runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Unknown("Formula2026"), 750)

            val result = repository.observeHighThresholdCelsius().first()
            val unknownEntry = result.entries.single { it.key is LmuWindowsVehicleClassData.Unknown }

            assertEquals(750, unknownEntry.value)
        }

    @Test
    fun `GTEのデフォルト値定数を用いてデフォルト閾値を検証できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT,
                repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `対象クラスの初期選択値はデフォルト値`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT,
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
            assertEquals(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 650)
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)
            listOf("温度{celsius}℃", "", " ", "過熱警告").forEach { text ->
                repository.saveReadoutText(text)
                assertEquals(text, repository.observeReadoutText().first())
                assertEquals(650, repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte])
                assertEquals(LmuWindowsVehicleClassData.Gte, repository.observeSelectedVehicleClass().first())
            }
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gt3)
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, 900)
            assertEquals("過熱警告", repository.observeReadoutText().first())
        }
}
