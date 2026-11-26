package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.manualreadout.usecase.DeleteManualReadoutSetUseCase
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.manualreadout.usecase.RetryManualReadoutHotkeyRegistrationUseCase
import kurou.kodriver.domain.manualreadout.usecase.UpdateManualReadoutSetUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 手動読み上げセット詳細の ViewModel を提供する。
 * 消費: 各シミュレーターの ManualReadoutSetPreferencesRepository、ホットキー関連 Repository、
 * TextToSpeechRepository、SoundVolumePreferencesRepository、および Narrator が登録する
 * named(simulatorId) の PlayStartSoundForKeyUseCase・SpeakTextUseCase。
 */
public val manualReadoutSetDetailModule: Module =
    module {
        viewModel { (simulatorId: String, setId: String) ->
            ManualReadoutSetDetailViewModel(
                simulatorId = simulatorId,
                setId = setId,
                checkTextToSpeechAvailable = CheckTextToSpeechAvailableUseCase(get()),
                observeSoundVolume = ObserveSoundVolumeUseCase(get()),
                playStartSoundForKey = get(named(simulatorId)),
                speakText = get(named(simulatorId)),
                observeSets = ObserveManualReadoutSetsUseCase(get(named(simulatorId))),
                updateSet = UpdateManualReadoutSetUseCase(get(named(simulatorId))),
                observeHotkeyRegistrationState = ObserveManualReadoutHotkeyRegistrationStateUseCase(get()),
                retryHotkeyRegistration = RetryManualReadoutHotkeyRegistrationUseCase(get()),
                deleteSet = DeleteManualReadoutSetUseCase(get(named(simulatorId))),
                observeOtherSimulatorSets =
                    Simulator.entries
                        .filter { it.id != simulatorId }
                        .associate { it.id to ObserveManualReadoutSetsUseCase(get(named(it.id))) },
            )
        }
    }
