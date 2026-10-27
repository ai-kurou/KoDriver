package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.acewindowsdata.AceWindowsAutoReadoutSettingsResetter
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsAutoReadoutSettingsResetterDataStoreTest {
    private val tempDir = Files.createTempDirectory("auto_readout_reset_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val flagDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("flag.pb") },
        )
    private val flagReadoutTextDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("flagReadoutText.pb") },
        )
    private val myBestLapDataStore =
        DataStoreFactory.create(
            serializer = MyBestLapPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("myBestLap.pb") },
        )
    private val remainingFuelLapsDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsRemainingFuelLapsPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingFuelLaps.pb") },
        )
    private val remainingFuelDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsRemainingFuelPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingFuel.pb") },
        )
    private val tyreTemperatureDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsTyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("tyreTemperature.pb") },
        )
    private val vehicleApproachDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsVehicleApproachPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleApproach.pb") },
        )
    private val resetter =
        AceWindowsAutoReadoutSettingsResetter(
            flagPreferencesRepository = AceWindowsFlagPreferencesRepositoryImpl(flagDataStore),
            flagReadoutTextPreferencesRepository =
                AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
                    flagReadoutTextDataStore,
                ),
            myBestLapPreferencesRepository = AceWindowsMyBestLapPreferencesRepositoryImpl(myBestLapDataStore),
            remainingFuelLapsPreferencesRepository =
                AceWindowsRemainingFuelLapsPreferencesRepositoryImpl(
                    remainingFuelLapsDataStore,
                ),
            remainingFuelPreferencesRepository =
                AceWindowsRemainingFuelPreferencesRepositoryImpl(
                    remainingFuelDataStore,
                ),
            tyreTemperaturePreferencesRepository =
                AceWindowsTyreTemperaturePreferencesRepositoryImpl(
                    tyreTemperatureDataStore,
                ),
            vehicleApproachPreferencesRepository =
                AceWindowsVehicleApproachPreferencesRepositoryImpl(
                    vehicleApproachDataStore,
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
            changeFlagSettings()
            changeFlagReadoutTextSettings()
            changeMyBestLapSettings()
            changeRemainingFuelLapsSettings()
            changeRemainingFuelSettings()
            changeTyreTemperatureSettings()
            changeVehicleApproachSettings()

            assertNotEquals(AceWindowsFlagPreferencesSerializer.defaultValue, flagDataStore.data.first())
            assertNotEquals(
                AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue,
                flagReadoutTextDataStore.data.first(),
            )
            assertNotEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertNotEquals(
                AceWindowsRemainingFuelLapsPreferencesSerializer.defaultValue,
                remainingFuelLapsDataStore.data.first(),
            )
            assertNotEquals(
                AceWindowsRemainingFuelPreferencesSerializer.defaultValue,
                remainingFuelDataStore.data.first(),
            )
            assertNotEquals(
                AceWindowsTyreTemperaturePreferencesSerializer.defaultValue,
                tyreTemperatureDataStore.data.first(),
            )
            assertNotEquals(
                AceWindowsVehicleApproachPreferencesSerializer.defaultValue,
                vehicleApproachDataStore.data.first(),
            )

            resetter.resetToDefault()

            assertEquals(AceWindowsFlagPreferencesSerializer.defaultValue, flagDataStore.data.first())
            assertEquals(
                AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue,
                flagReadoutTextDataStore.data.first(),
            )
            assertEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertEquals(
                AceWindowsRemainingFuelLapsPreferencesSerializer.defaultValue,
                remainingFuelLapsDataStore.data.first(),
            )
            assertEquals(AceWindowsRemainingFuelPreferencesSerializer.defaultValue, remainingFuelDataStore.data.first())
            assertEquals(
                AceWindowsTyreTemperaturePreferencesSerializer.defaultValue,
                tyreTemperatureDataStore.data.first(),
            )
            assertEquals(
                AceWindowsVehicleApproachPreferencesSerializer.defaultValue,
                vehicleApproachDataStore.data.first(),
            )
        }

    private suspend fun changeFlagSettings() {
        flagDataStore.updateData {
            it.copy(
                enabledStates = mapOf("saved_key" to false),
            )
        }
    }

    private suspend fun changeFlagReadoutTextSettings() {
        flagReadoutTextDataStore.updateData {
            it.copy(
                checkeredFlagText = "変更した文言",
                whiteFlagText = "変更した文言",
                greenFlagText = "変更した文言",
                redFlagText = "変更した文言",
                blueFlagText = "変更した文言",
                yellowFlagText = "変更した文言",
                blackFlagText = "変更した文言",
                blackWhiteFlagText = "変更した文言",
                orangeCircleFlagText = "変更した文言",
                redYellowStripesFlagText = "変更した文言",
            )
        }
    }

    private suspend fun changeMyBestLapSettings() {
        myBestLapDataStore.updateData {
            it.copy(
                voiceType = "変更した文言",
                aceWindowsReadoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeRemainingFuelLapsSettings() {
        remainingFuelLapsDataStore.updateData {
            it.copy(
                thresholdLaps = it.thresholdLaps + 1,
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

    private suspend fun changeVehicleApproachSettings() {
        vehicleApproachDataStore.updateData {
            it.copy(
                thresholdMeters = it.thresholdMeters + 1.0,
                enabledStates = mapOf("saved_key" to false),
                readoutText = "変更した文言",
            )
        }
    }
}
