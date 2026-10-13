package kurou.kodriver.feature.acewindowsautoreadout.mybestlapdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ACE 自己ベストラップアナウンス詳細設定（ace-windows-auto-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: AceWindowsAutoReadoutMyBestLapDetailViewModel。自己ベストラップ更新の有効/無効・文言の永続化用 UseCase を解決する。
 * 試聴は ace-windows-narrator が提供する ACE 修飾子付き PlayStartSoundForKeyUseCase・SpeakTextUseCase を利用する。
 */
val aceWindowsAutoReadoutMyBestLapDetailModule =
    module {
        viewModel {
            AceWindowsAutoReadoutMyBestLapDetailViewModel(get(), get())
        }

        factory {
            MyBestLapReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.AceWindows.id)),
                get(named(Simulator.AceWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveAceWindowsMyBestLapReadoutTextUseCase)
        factoryOf(::SaveAceWindowsMyBestLapReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { MyBestLapUseCases(get(), get()) }
        factoryOf(::ObserveAutoReadoutEnabledStatesUseCase)
        factoryOf(::SaveAutoReadoutEnabledStateUseCase)
    }
