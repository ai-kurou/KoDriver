@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_vehicle_class_tyre_temperature_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は全クラス分のデフォルトしきい値`() =
        testScope.runTest {
            val expected =
                lmuWindowsAllVehicleClasses.associateWith {
                    lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(it)
                }

            assertEquals(expected, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `saveHighThresholdCelsius で保存したクラスの値だけが更新され他クラスはデフォルトのまま`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 110)

            val result = repository.observeHighThresholdCelsius().first()

            assertEquals(110, result[LmuWindowsVehicleClassData.Gte])
            assertEquals(
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gt3),
                result[LmuWindowsVehicleClassData.Gt3],
            )
        }

    @Test
    fun `saveHighThresholdCelsius を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 80)
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 100)

            assertEquals(
                100,
                repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `Unknownクラスは raw 値によらず1つのしきい値を共有する`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Unknown("Formula2026"), 105)

            val result = repository.observeHighThresholdCelsius().first()
            val unknownEntry = result.entries.single { it.key is LmuWindowsVehicleClassData.Unknown }

            assertEquals(105, unknownEntry.value)
        }

    @Test
    fun `GTEのデフォルト値定数を用いてデフォルトしきい値を検証できる`() =
        testScope.runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT,
                repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `対象クラスの初期選択値はデフォルト値`() =
        testScope.runTest {
            assertEquals(
                LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT,
                repository.observeSelectedVehicleClass().first(),
            )
        }

    @Test
    fun `saveSelectedVehicleClass で保存したクラスが選択値として反映される`() =
        testScope.runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)

            assertEquals(LmuWindowsVehicleClassData.Gte, repository.observeSelectedVehicleClass().first())
        }

    @Test
    fun `saveSelectedVehicleClass を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gt3)

            assertEquals(LmuWindowsVehicleClassData.Gt3, repository.observeSelectedVehicleClass().first())
        }

    @Test
    fun `選択クラスがUnknownの場合は代表キーとして保存・復元される`() =
        testScope.runTest {
            repository.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Unknown("Formula2026"))

            val result = repository.observeSelectedVehicleClass().first()

            assertEquals(true, result is LmuWindowsVehicleClassData.Unknown)
        }
}
