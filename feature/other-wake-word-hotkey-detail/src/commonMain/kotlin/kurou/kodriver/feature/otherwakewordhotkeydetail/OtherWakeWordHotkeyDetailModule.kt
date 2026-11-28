package kurou.kodriver.feature.otherwakewordhotkeydetail

import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.speechrecognition.usecase.ObserveSpeechRecognitionEnabledUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyUseCase
import kurou.kodriver.domain.speechrecognition.usecase.RetryWakeWordHotkeyRegistrationUseCase
import kurou.kodriver.domain.speechrecognition.usecase.SaveWakeWordHotkeyUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ウェイクワードホットキー詳細の ViewModel を提供する。
 * 消費: WakeWordHotkeyPreferencesRepository、WakeWordHotkeyRepository、
 * SpeechRecognitionEnabledRepository、named(simulatorId) の ManualReadoutSetPreferencesRepository。
 */
public val otherWakeWordHotkeyDetailModule: Module =
    module {
        viewModel {
            OtherWakeWordHotkeyDetailViewModel(
                observeHotkey = ObserveWakeWordHotkeyUseCase(get()),
                saveHotkey = SaveWakeWordHotkeyUseCase(get()),
                observeRegistrationState = ObserveWakeWordHotkeyRegistrationStateUseCase(get()),
                retryRegistration = RetryWakeWordHotkeyRegistrationUseCase(get()),
                observeManualReadoutSets =
                    Simulator.entries.associate {
                        it.id to ObserveManualReadoutSetsUseCase(get(named(it.id)))
                    },
                observeSpeechRecognitionEnabled = ObserveSpeechRecognitionEnabledUseCase(get()),
            )
        }
    }
