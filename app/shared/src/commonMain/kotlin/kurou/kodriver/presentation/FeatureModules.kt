package kurou.kodriver.presentation

import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.aceWindowsAutoReadoutFlagDetailModule
import kurou.kodriver.feature.acewindowsautoreadout.mybestlapdetail.aceWindowsAutoReadoutMyBestLapDetailModule
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.aceWindowsAutoReadoutRemainingFuelDetailModule
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.aceWindowsAutoReadoutRemainingFuelLapsDetailModule
import kurou.kodriver.feature.acewindowsautoreadout.tyretemperaturedetail.aceWindowsAutoReadoutTyreTemperatureDetailModule
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.aceWindowsAutoReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.acewindowsconnection.aceWindowsConnectionModule
import kurou.kodriver.feature.acewindowsdebugstatedetail.aceWindowsDebugStateDetailModule
import kurou.kodriver.feature.acewindowsnarrator.aceWindowsNarratorModule
import kurou.kodriver.feature.autoreadoutlist.autoReadoutListModule
import kurou.kodriver.feature.gt7ps5autoreadout.mybestlapdetail.gt7Ps5AutoReadoutMyBestLapDetailModule
import kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail.gt7Ps5AutoReadoutRemainingFuelDetailModule
import kurou.kodriver.feature.gt7ps5autoreadout.remainingfuellapsdetail.gt7Ps5AutoReadoutRemainingFuelLapsDetailModule
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.gt7Ps5AutoReadoutTyreTemperatureDetailModule
import kurou.kodriver.feature.gt7ps5connection.gt7Ps5ConnectionModule
import kurou.kodriver.feature.gt7ps5debugstatedetail.gt7Ps5DebugStateDetailModule
import kurou.kodriver.feature.gt7ps5narrator.gt7Ps5NarratorModule
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.lmuWindowsAutoReadoutBrakeTemperatureDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.lmuWindowsAutoReadoutBrakeWearDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail.lmuWindowsAutoReadoutFlagDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail.lmuWindowsAutoReadoutMyBestLapDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail.lmuWindowsAutoReadoutPitTimingDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail.lmuWindowsAutoReadoutRemainingVirtualEnergyDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.lmuWindowsAutoReadoutTyreTemperatureDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.tyreweardetail.lmuWindowsAutoReadoutTyreWearDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail.lmuWindowsAutoReadoutVehicleApproachDetailModule
import kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail.lmuWindowsAutoReadoutVehicleDamageDetailModule
import kurou.kodriver.feature.lmuwindowsconnection.lmuWindowsConnectionModule
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.lmuWindowsDebugStateDetailModule
import kurou.kodriver.feature.lmuwindowsnarrator.lmuWindowsNarratorModule
import kurou.kodriver.feature.main.mainModule
import kurou.kodriver.feature.main.mainPlatformModule
import kurou.kodriver.feature.manualreadoutlist.manualReadoutListModule
import kurou.kodriver.feature.narratoroverlay.narratorOverlayModule
import kurou.kodriver.feature.otherconsoleipdetail.otherConsoleIpDetailModule
import kurou.kodriver.feature.otherfeedbackdetail.otherFeedbackDetailModule
import kurou.kodriver.feature.otherlist.otherListModule
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.otherOverlayBackgroundOpacityDetailModule
import kurou.kodriver.feature.otheroverlaytextcolordetail.otherOverlayTextColorDetailModule
import kurou.kodriver.feature.otheroverlaytextsizedetail.otherOverlayTextSizeDetailModule
import kurou.kodriver.feature.otherreadoutstartsounddetail.otherReadoutStartSoundDetailModule
import kurou.kodriver.feature.otherserveripdetail.otherServerIpDetailModule
import kurou.kodriver.feature.otherthemedetail.otherThemeDetailModule
import kurou.kodriver.feature.othervoicedetail.otherVoiceDetailModule
import kurou.kodriver.feature.othervoicepitchdetail.otherVoicePitchDetailModule
import kurou.kodriver.feature.othervoicespeeddetail.otherVoiceSpeedDetailModule
import kurou.kodriver.feature.othervolumedetail.otherVolumeDetailModule
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
val featureModules: List<Module> =
    listOf(
        mainModule,
        mainPlatformModule,
        lmuWindowsConnectionModule,
        gt7Ps5AutoReadoutMyBestLapDetailModule,
        gt7Ps5AutoReadoutRemainingFuelDetailModule,
        gt7Ps5AutoReadoutRemainingFuelLapsDetailModule,
        gt7Ps5AutoReadoutTyreTemperatureDetailModule,
        gt7Ps5ConnectionModule,
        aceWindowsConnectionModule,
        serverConnectionModule,
        lmuWindowsNarratorModule,
        gt7Ps5NarratorModule,
        aceWindowsNarratorModule,
        lmuWindowsDebugStateDetailModule,
        gt7Ps5DebugStateDetailModule,
        aceWindowsDebugStateDetailModule,
        otherListModule,
        otherFeedbackDetailModule,
        otherReadoutStartSoundDetailModule,
        otherThemeDetailModule,
        otherOverlayTextColorDetailModule,
        otherOverlayTextSizeDetailModule,
        otherOverlayBackgroundOpacityDetailModule,
        otherServerIpDetailModule,
        otherConsoleIpDetailModule,
        otherVolumeDetailModule,
        otherVoicePitchDetailModule,
        otherVoiceSpeedDetailModule,
        otherVoiceDetailModule,
        autoReadoutListModule,
        manualReadoutListModule,
        telemetryLogListModule,
        telemetryLogDetailModule,
        lmuWindowsAutoReadoutVehicleApproachDetailModule,
        lmuWindowsAutoReadoutFlagDetailModule,
        lmuWindowsAutoReadoutMyBestLapDetailModule,
        lmuWindowsAutoReadoutVehicleDamageDetailModule,
        lmuWindowsAutoReadoutTyreTemperatureDetailModule,
        lmuWindowsAutoReadoutRemainingVirtualEnergyDetailModule,
        lmuWindowsAutoReadoutTyreWearDetailModule,
        lmuWindowsAutoReadoutBrakeTemperatureDetailModule,
        lmuWindowsAutoReadoutBrakeWearDetailModule,
        lmuWindowsAutoReadoutPitTimingDetailModule,
        aceWindowsAutoReadoutRemainingFuelDetailModule,
        aceWindowsAutoReadoutFlagDetailModule,
        aceWindowsAutoReadoutTyreTemperatureDetailModule,
        aceWindowsAutoReadoutVehicleApproachDetailModule,
        aceWindowsAutoReadoutMyBestLapDetailModule,
        aceWindowsAutoReadoutRemainingFuelLapsDetailModule,
        narratorOverlayModule,
    )
