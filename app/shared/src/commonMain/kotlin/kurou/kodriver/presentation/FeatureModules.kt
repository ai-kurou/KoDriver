package kurou.kodriver.presentation

import kurou.kodriver.feature.acewindowsconnection.aceWindowsConnectionModule
import kurou.kodriver.feature.acewindowsnarrator.aceWindowsNarratorModule
import kurou.kodriver.feature.acewindowsreadout.remainingfueldetail.aceWindowsReadoutRemainingFuelDetailModule
import kurou.kodriver.feature.debugstatedetail.debugStateDetailModule
import kurou.kodriver.feature.gt7ps5connection.gt7Ps5ConnectionModule
import kurou.kodriver.feature.gt7ps5narrator.gt7Ps5NarratorModule
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.gt7Ps5ReadoutMyBestLapDetailModule
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.gt7Ps5ReadoutRemainingFuelLapsDetailModule
import kurou.kodriver.feature.lmuwindowsconnection.lmuWindowsConnectionModule
import kurou.kodriver.feature.lmuwindowsnarrator.lmuWindowsNarratorModule
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.lmuWindowsReadoutFlagDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.lmuWindowsReadoutMyBestLapDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.lmuWindowsReadoutPitTimingDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.lmuWindowsReadoutRemainingVirtualEnergyDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.lmuWindowsReadoutTyreTemperatureDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail.lmuWindowsReadoutTyreWearDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.lmuWindowsReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.lmuWindowsReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.main.mainModule
import kurou.kodriver.feature.main.mainPlatformModule
import kurou.kodriver.feature.otherconsoleipdetail.otherConsoleIpDetailModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.otherreadoutstartsounddetail.otherReadoutStartSoundDetailModule
import kurou.kodriver.feature.otherserveripdetail.otherServerIpDetailModule
import kurou.kodriver.feature.otherthemedetail.otherThemeDetailModule
import kurou.kodriver.feature.othervolumedetail.otherVolumeDetailModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import kurou.kodriver.feature.serverconnection.serverConnectionModule
import kurou.kodriver.feature.telemetrylogdetail.telemetryLogDetailModule
import kurou.kodriver.feature.telemetryloglist.telemetryLogListModule
import org.koin.core.module.Module

/**
 * `:feature:*` 各モジュールの Koin モジュールを束ねたリスト。
 *
 * ここに含まれるのは feature 層のモジュールのみ。データ層（`:core:*data`）とアプリバージョン定数は
 * app エントリーポイント（main.kt / KoDriverApplication.kt）の composition root で別途束ねられる。
 * `:app:shared` は `:core:*` へ依存できない（moduleGraphAssert の `:app:shared -X> :core:.*`）ため、
 * このリストは feature only になる。
 */
val featureModules: List<Module> = listOf(
    mainModule,
    mainPlatformModule,
    lmuWindowsConnectionModule,
    gt7Ps5ConnectionModule,
    aceWindowsConnectionModule,
    serverConnectionModule,
    lmuWindowsNarratorModule,
    gt7Ps5NarratorModule,
    aceWindowsNarratorModule,
    otherListModule,
    otherReadoutStartSoundDetailModule,
    otherThemeDetailModule,
    otherServerIpDetailModule,
    otherConsoleIpDetailModule,
    otherVolumeDetailModule,
    readoutListModule,
    telemetryLogListModule,
    telemetryLogDetailModule,
    lmuWindowsReadoutVehicleApproachDetailModule,
    lmuWindowsReadoutFlagDetailModule,
    lmuWindowsReadoutMyBestLapDetailModule,
    lmuWindowsReadoutVehicleDamageDetailModule,
    lmuWindowsReadoutTyreTemperatureDetailModule,
    lmuWindowsReadoutRemainingVirtualEnergyDetailModule,
    lmuWindowsReadoutTyreWearDetailModule,
    lmuWindowsReadoutPitTimingDetailModule,
    gt7Ps5ReadoutMyBestLapDetailModule,
    gt7Ps5ReadoutRemainingFuelLapsDetailModule,
    aceWindowsReadoutRemainingFuelDetailModule,
    debugStateDetailModule,
)
