package kurou.kodriver.feature.speechrecognitionlist

import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** 音声タブの一覧の ViewModel を提供する Koin モジュール。 */
val speechRecognitionListModule =
    module {
        viewModel {
            SpeechRecognitionListViewModel(
                observeSpeechCommandGuide = ObserveSpeechCommandGuideUseCase(get(), get()),
            )
        }
    }
