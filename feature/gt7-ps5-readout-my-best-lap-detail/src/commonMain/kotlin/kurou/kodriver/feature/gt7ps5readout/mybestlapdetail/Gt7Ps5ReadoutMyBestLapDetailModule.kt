package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * GT7 自己ベストラップアナウンス詳細設定（gt7-ps5-readout-my-best-lap-detail feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ReadoutMyBestLapDetailViewModel。自己ベストラップ更新の有効/無効・文言の永続化用 UseCase を解決する。
 * 試聴は gt7-ps5-narrator が提供する GT7 修飾子付き PlayStartSoundForKeyUseCase・SpeakTextUseCase を利用する。
 */
val gt7Ps5ReadoutMyBestLapDetailModule =
    module {
        viewModel {
            Gt7Ps5ReadoutMyBestLapDetailViewModel(get(), get())
        }

        factory {
            MyBestLapReadoutUseCases(
                get(),
                get(),
                get(named(Simulator.Gt7Ps5.id)),
                get(named(Simulator.Gt7Ps5.id)),
                get(),
                get(),
            )
        }
        factoryOf(::ObserveGt7Ps5MyBestLapReadoutTextUseCase)
        factoryOf(::SaveGt7Ps5MyBestLapReadoutTextUseCase)
        factoryOf(::CheckTextToSpeechAvailableUseCase)
        factoryOf(::ObserveSoundVolumeUseCase)
        factory { MyBestLapUseCases(get(), get()) }
        factoryOf(::ObserveReadoutEnabledStatesUseCase)
        factoryOf(::SaveReadoutEnabledStateUseCase)
    }
