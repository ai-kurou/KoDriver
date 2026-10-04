package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * LMU 自己ベストラップアナウンス詳細設定（lmu-windows-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutMyBestLapDetailViewModel。自己ベストラップ更新の有効/無効・文言の永続化用 UseCase を解決する。
 * 試聴は lmu-windows-narrator が提供する LMU 修飾子付き PlayStartSoundForKeyUseCase・SpeakTextUseCase を利用する。
 */
val lmuWindowsReadoutMyBestLapDetailModule =
    module {
        viewModel {
            LmuWindowsReadoutMyBestLapDetailViewModel(get(), get())
        }

        factory {
            MyBestLapReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.LmuWindows.id)),
                get(named(Simulator.LmuWindows.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveLmuWindowsMyBestLapReadoutTextUseCase)
        factoryOf(::SaveLmuWindowsMyBestLapReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { MyBestLapUseCases(get(), get()) }
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
    }
