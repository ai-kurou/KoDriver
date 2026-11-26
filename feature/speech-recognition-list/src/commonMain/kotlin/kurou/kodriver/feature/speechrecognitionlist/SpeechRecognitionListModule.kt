package kurou.kodriver.feature.speechrecognitionlist

import kurou.kodriver.domain.manualreadout.usecase.ObserveManualReadoutItemSpokenUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** 音声タブの一覧の ViewModel を提供する Koin モジュール。 */
public val speechRecognitionListModule: Module =
    module {
        viewModel {
            SpeechRecognitionListViewModel(
                observeSpeechCommandGuide = ObserveSpeechCommandGuideUseCase(get(), get()),
                observeItemSpoken = ObserveManualReadoutItemSpokenUseCase(get()),
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(get()),
            )
        }
    }
