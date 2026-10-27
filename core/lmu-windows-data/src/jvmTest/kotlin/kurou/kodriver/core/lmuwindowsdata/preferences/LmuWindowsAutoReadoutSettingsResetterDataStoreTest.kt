package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.lmuwindowsdata.LmuWindowsAutoReadoutSettingsResetter
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutSettingsResetterDataStoreTest {
    private val tempDir = Files.createTempDirectory("auto_readout_reset_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val flagDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsFlagPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("flag.pb") },
        )
    private val flagReadoutTextDataStore =
        DataStoreFactory.create(
            serializer = FlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("flagReadoutText.pb") },
        )
    private val myBestLapDataStore =
        DataStoreFactory.create(
            serializer = MyBestLapPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("myBestLap.pb") },
        )
    private val pitTimingDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsPitTimingPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("pitTiming.pb") },
        )
    private val remainingFuelDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsRemainingFuelPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingFuel.pb") },
        )
    private val remainingVirtualEnergyDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsRemainingVirtualEnergyPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remainingVirtualEnergy.pb") },
        )
    private val tyreTemperatureDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("tyreTemperature.pb") },
        )
    private val tyreWearDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsTyreWearPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("tyreWear.pb") },
        )
    private val vehicleApproachDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleApproachPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleApproach.pb") },
        )
    private val vehicleApproachThresholdsDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleApproachThresholdsPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleApproachThresholds.pb") },
        )
    private val vehicleClassBrakeTemperatureDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleClassBrakeTemperature.pb") },
        )
    private val vehicleClassBrakeWearDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassBrakeWearPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleClassBrakeWear.pb") },
        )
    private val vehicleClassTyreTemperatureDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleClassTyreTemperature.pb") },
        )
    private val vehicleDamageDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleDamagePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("vehicleDamage.pb") },
        )
    private val resetter =
        LmuWindowsAutoReadoutSettingsResetter(
            flagPreferencesRepository = LmuWindowsFlagPreferencesRepositoryImpl(flagDataStore),
            flagReadoutTextPreferencesRepository =
                LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(
                    flagReadoutTextDataStore,
                ),
            myBestLapPreferencesRepository = LmuWindowsMyBestLapPreferencesRepositoryImpl(myBestLapDataStore),
            pitTimingPreferencesRepository = LmuWindowsPitTimingPreferencesRepositoryImpl(pitTimingDataStore),
            remainingFuelPreferencesRepository =
                LmuWindowsRemainingFuelPreferencesRepositoryImpl(
                    remainingFuelDataStore,
                ),
            remainingVirtualEnergyPreferencesRepository =
                LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
                    remainingVirtualEnergyDataStore,
                ),
            tyreTemperaturePreferencesRepository =
                LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
                    tyreTemperatureDataStore,
                ),
            tyreWearPreferencesRepository = LmuWindowsTyreWearPreferencesRepositoryImpl(tyreWearDataStore),
            vehicleApproachPreferencesRepository =
                LmuWindowsVehicleApproachPreferencesRepositoryImpl(
                    vehicleApproachDataStore,
                ),
            vehicleApproachThresholdsPreferencesRepository =
                LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl(
                    vehicleApproachThresholdsDataStore,
                ),
            vehicleClassBrakeTemperaturePreferencesRepository =
                LmuWindowsVehicleClassBrakeTemperaturePreferencesRepositoryImpl(
                    vehicleClassBrakeTemperatureDataStore,
                ),
            vehicleClassBrakeWearPreferencesRepository =
                LmuWindowsVehicleClassBrakeWearPreferencesRepositoryImpl(
                    vehicleClassBrakeWearDataStore,
                ),
            vehicleClassTyreTemperaturePreferencesRepository =
                LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryImpl(
                    vehicleClassTyreTemperatureDataStore,
                ),
            vehicleDamagePreferencesRepository =
                LmuWindowsVehicleDamagePreferencesRepositoryImpl(
                    vehicleDamageDataStore,
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
            changePitTimingSettings()
            changeRemainingFuelSettings()
            changeRemainingVirtualEnergySettings()
            changeTyreTemperatureSettings()
            changeTyreWearSettings()
            changeVehicleApproachSettings()
            changeVehicleApproachThresholdsSettings()
            changeVehicleClassBrakeTemperatureSettings()
            changeVehicleClassBrakeWearSettings()
            changeVehicleClassTyreTemperatureSettings()
            changeVehicleDamageSettings()

            assertNotEquals(LmuWindowsFlagPreferencesSerializer.defaultValue, flagDataStore.data.first())
            assertNotEquals(FlagReadoutTextPreferencesSerializer.defaultValue, flagReadoutTextDataStore.data.first())
            assertNotEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertNotEquals(LmuWindowsPitTimingPreferencesSerializer.defaultValue, pitTimingDataStore.data.first())
            assertNotEquals(
                LmuWindowsRemainingFuelPreferencesSerializer.defaultValue,
                remainingFuelDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.defaultValue,
                remainingVirtualEnergyDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue,
                tyreTemperatureDataStore.data.first(),
            )
            assertNotEquals(LmuWindowsTyreWearPreferencesSerializer.defaultValue, tyreWearDataStore.data.first())
            assertNotEquals(
                LmuWindowsVehicleApproachPreferencesSerializer.defaultValue,
                vehicleApproachDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsVehicleApproachThresholdsPreferencesSerializer.defaultValue,
                vehicleApproachThresholdsDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.defaultValue,
                vehicleClassBrakeTemperatureDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsVehicleClassBrakeWearPreferencesSerializer.defaultValue,
                vehicleClassBrakeWearDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.defaultValue,
                vehicleClassTyreTemperatureDataStore.data.first(),
            )
            assertNotEquals(
                LmuWindowsVehicleDamagePreferencesSerializer.defaultValue,
                vehicleDamageDataStore.data.first(),
            )

            resetter.resetToDefault()

            assertEquals(LmuWindowsFlagPreferencesSerializer.defaultValue, flagDataStore.data.first())
            assertEquals(FlagReadoutTextPreferencesSerializer.defaultValue, flagReadoutTextDataStore.data.first())
            assertEquals(MyBestLapPreferencesSerializer.defaultValue, myBestLapDataStore.data.first())
            assertEquals(LmuWindowsPitTimingPreferencesSerializer.defaultValue, pitTimingDataStore.data.first())
            assertEquals(LmuWindowsRemainingFuelPreferencesSerializer.defaultValue, remainingFuelDataStore.data.first())
            assertEquals(
                LmuWindowsRemainingVirtualEnergyPreferencesSerializer.defaultValue,
                remainingVirtualEnergyDataStore.data.first(),
            )
            assertEquals(
                LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue,
                tyreTemperatureDataStore.data.first(),
            )
            assertEquals(LmuWindowsTyreWearPreferencesSerializer.defaultValue, tyreWearDataStore.data.first())
            assertEquals(
                LmuWindowsVehicleApproachPreferencesSerializer.defaultValue,
                vehicleApproachDataStore.data.first(),
            )
            assertEquals(
                LmuWindowsVehicleApproachThresholdsPreferencesSerializer.defaultValue,
                vehicleApproachThresholdsDataStore.data.first(),
            )
            assertEquals(
                LmuWindowsVehicleClassBrakeTemperaturePreferencesSerializer.defaultValue,
                vehicleClassBrakeTemperatureDataStore.data.first(),
            )
            assertEquals(
                LmuWindowsVehicleClassBrakeWearPreferencesSerializer.defaultValue,
                vehicleClassBrakeWearDataStore.data.first(),
            )
            assertEquals(
                LmuWindowsVehicleClassTyreTemperaturePreferencesSerializer.defaultValue,
                vehicleClassTyreTemperatureDataStore.data.first(),
            )
            assertEquals(LmuWindowsVehicleDamagePreferencesSerializer.defaultValue, vehicleDamageDataStore.data.first())
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
                sectorYellowFlagText = "変更した文言",
                blueFlagText = "変更した文言",
                fullCourseYellowFlagText = "変更した文言",
                redFlagText = "変更した文言",
            )
        }
    }

    private suspend fun changeMyBestLapSettings() {
        myBestLapDataStore.updateData {
            it.copy(
                voiceType = "変更した文言",
                lmuWindowsReadoutText = "変更した文言",
            )
        }
    }

    private suspend fun changePitTimingSettings() {
        pitTimingDataStore.updateData {
            it.copy(
                virtualEnergyLaps = it.virtualEnergyLaps + 1,
                tyreWearLaps = it.tyreWearLaps + 1,
                enabledStates = mapOf("saved_key" to false),
                virtualEnergyReadoutText = "変更した文言",
                virtualEnergyImminentReadoutText = "変更した文言",
                tyreWearReadoutText = "変更した文言",
                tyreWearImminentReadoutText = "変更した文言",
                fuelLaps = it.fuelLaps + 1,
                fuelReadoutText = "変更した文言",
                fuelImminentReadoutText = "変更した文言",
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

    private suspend fun changeRemainingVirtualEnergySettings() {
        remainingVirtualEnergyDataStore.updateData {
            it.copy(
                thresholdPercentage = it.thresholdPercentage + 1,
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeTyreTemperatureSettings() {
        tyreTemperatureDataStore.updateData {
            it.copy(
                enabledStates = mapOf("saved_key" to false),
                lowWarningPhases = mapOf(1 to false),
                overheatReadoutText = "変更した文言",
                coldReadoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeTyreWearSettings() {
        tyreWearDataStore.updateData {
            it.copy(
                thresholdPercentage = it.thresholdPercentage + 1,
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeVehicleApproachSettings() {
        vehicleApproachDataStore.updateData {
            it.copy(
                skipFirstLap = !it.skipFirstLap,
                enabledStates = mapOf("saved_key" to false),
                startLeftReadoutText = "変更した文言",
                startRightReadoutText = "変更した文言",
                sustainedLeftReadoutText = "変更した文言",
                sustainedRightReadoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeVehicleApproachThresholdsSettings() {
        vehicleApproachThresholdsDataStore.updateData {
            it.copy(
                longitudinalThresholdMeters = it.longitudinalThresholdMeters + 1.0,
                lateralThresholdMeters = it.lateralThresholdMeters + 1.0,
                sustainedApproachDurationSeconds = it.sustainedApproachDurationSeconds + 1,
            )
        }
    }

    private suspend fun changeVehicleClassBrakeTemperatureSettings() {
        vehicleClassBrakeTemperatureDataStore.updateData {
            it.copy(
                highThresholdCelsiusByVehicleClass = mapOf("GTE" to 177),
                selectedVehicleClassKey = "変更した文言",
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeVehicleClassBrakeWearSettings() {
        vehicleClassBrakeWearDataStore.updateData {
            it.copy(
                lowThresholdPercentByVehicleClass = mapOf("GTE" to 177),
                selectedVehicleClassKey = "変更した文言",
                readoutText = "変更した文言",
            )
        }
    }

    private suspend fun changeVehicleClassTyreTemperatureSettings() {
        vehicleClassTyreTemperatureDataStore.updateData {
            it.copy(
                highThresholdCelsiusByVehicleClass = mapOf("GTE" to 177),
                selectedVehicleClassKey = "変更した文言",
            )
        }
    }

    private suspend fun changeVehicleDamageSettings() {
        vehicleDamageDataStore.updateData {
            it.copy(
                enabledStates = mapOf("saved_key" to false),
                overheatReadoutText = "変更した文言",
                partDetachedReadoutText = "変更した文言",
                tyreDetachedReadoutText = "変更した文言",
            )
        }
    }
}
