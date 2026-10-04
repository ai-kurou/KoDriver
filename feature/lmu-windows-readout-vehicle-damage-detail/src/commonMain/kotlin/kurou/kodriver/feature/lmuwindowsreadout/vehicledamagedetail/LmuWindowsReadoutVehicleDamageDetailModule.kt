package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 車両故障アナウンスの詳細設定。文言・スイッチは VehicleDamagePreferencesRepository に保存する。
 * TTS利用可否・音量は :core:data、試聴用の PlaySpeechEventUseCase は narrator モジュールから解決する。
 */
val lmuWindowsReadoutVehicleDamageDetailModule =
    module {
        viewModel { LmuWindowsReadoutVehicleDamageDetailViewModel(get(), get()) }
        factory { VehicleDamageUseCases(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { VehicleDamageReadoutUseCases(get(named(Simulator.LmuWindows.id)), get(), get()) }
        factoryOf(::ObserveLmuWindowsVehicleDamageEnabledStatesUseCase)
        factoryOf(::SaveLmuWindowsVehicleDamageEnabledStateUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factoryOf(::ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsVehicleDamageOverheatReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase)
        factoryOf(::ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase)
    }
