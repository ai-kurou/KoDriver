package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.gt7ps5data.Gt7Ps5AutoReadoutSettingsResetter
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5AutoReadoutSettingsResetterDataStoreTest {
    private val tempDir = Files.createTempDirectory("auto_readout_reset_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val myBestLapDataStore =
        DataStoreFactory.create(
            serializer = MyBestLapPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("myBestLap.pb") },
        )
    private val remainingFuelLapsDataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5RemainingFuelLapsPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingFuelLaps.pb") },
        )
    private val remainingFuelDataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5RemainingFuelPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingFuel.pb") },
        )
    private val tyreTemperatureDataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5TyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("tyreTemperature.pb") },
        )
    private val resetter =
        Gt7Ps5AutoReadoutSettingsResetter(
            myBestLapPreferencesRepository = Gt7Ps5MyBestLapPreferencesRepositoryImpl(myBestLapDataStore),
            remainingFuelLapsPreferencesRepository =
                Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
                    remainingFuelLapsDataStore,
                ),
            remainingFuelPreferencesRepository =
                Gt7Ps5RemainingFuelPreferencesRepositoryImpl(
                    remainingFuelDataStore,
                ),
            tyreTemperaturePreferencesRepository =
                Gt7Ps5TyreTemperaturePreferencesRepositoryImpl(
                    tyreTemperatureDataStore,
                ),
        )

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `全対象のDataStoreを既定値へ戻す`() =
        runTest {
            changeMyBestLapSettings()
            changeRemainingFuelLapsSettings()
            changeRemainingFuelSettings()
            changeTyreTemperatureSettings()

            assertNotEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertNotEquals(
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.defaultValue,
                remainingFuelLapsDataStore.data.first(),
            )
            assertNotEquals(Gt7Ps5RemainingFuelPreferencesSerializer.defaultValue, remainingFuelDataStore.data.first())
            assertNotEquals(
                Gt7Ps5TyreTemperaturePreferencesSerializer.defaultValue,
                tyreTemperatureDataStore.data.first(),
            )

            resetter.resetToDefault()

            assertEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertEquals(
                Gt7Ps5RemainingFuelLapsPreferencesSerializer.defaultValue,
                remainingFuelLapsDataStore.data.first(),
            )
            assertEquals(Gt7Ps5RemainingFuelPreferencesSerializer.defaultValue, remainingFuelDataStore.data.first())
            assertEquals(Gt7Ps5TyreTemperaturePreferencesSerializer.defaultValue, tyreTemperatureDataStore.data.first())
        }

    private suspend fun changeMyBestLapSettings() {
        myBestLapDataStore.updateData {
            it.copy(
                voiceType = "変更した文言",
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeRemainingFuelLapsSettings() {
        remainingFuelLapsDataStore.updateData {
            it.copy(
                remainingFuelLaps = it.remainingFuelLaps + 1,
                readoutText = "変更した文言",
                emptyReadoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeRemainingFuelSettings() {
        remainingFuelDataStore.updateData {
            it.copy(
                thresholdPercentage = it.thresholdPercentage + 1,
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeTyreTemperatureSettings() {
        tyreTemperatureDataStore.updateData {
            it.copy(
                highThresholdCelsius = it.highThresholdCelsius + 1,
                enabledStates = mapOf("saved_key" to false),
                overheatReadoutText = "変更した文言",
            )
        }
    }
}
