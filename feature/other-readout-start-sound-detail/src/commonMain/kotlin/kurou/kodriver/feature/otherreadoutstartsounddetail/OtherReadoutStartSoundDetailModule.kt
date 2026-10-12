package kurou.kodriver.feature.otherreadoutstartsounddetail

import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.PreviewStartSoundUseCase
import kurou.kodriver.domain.usecase.SaveReadoutStartSoundTypeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * 読み上げ開始音設定詳細（other-readout-start-sound-detail feature）の Koin モジュール。
 *
 * 提供: OtherReadoutStartSoundDetailViewModel と ObserveReadoutStartSoundTypeUseCase。
 * 消費（get で解決）: ReadoutStartSoundPreferencesRepository（:core:data）、および試聴用の
 *   選択中の simulatorId に対応する named(simulatorId) の TextToSpeechEngine（各 narrator feature で登録）。
 */
val otherReadoutStartSoundDetailModule =
    module {
        // ViewModel
        viewModel { (simulatorId: String) ->
            OtherReadoutStartSoundDetailViewModel(
                observeReadoutStartSoundType = ObserveReadoutStartSoundTypeUseCase(get()),
                saveReadoutStartSoundType = SaveReadoutStartSoundTypeUseCase(get()),
                previewStartSound = PreviewStartSoundUseCase(get(named(simulatorId))),
            )
        }

        // 他の feature も Koin 経由で使用する設定観測 UseCase。設定は全シミュレーターで共有する。
        factory { ObserveReadoutStartSoundTypeUseCase(get()) }
    }
