package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * オーバーレイ表示機能（narrator-overlay feature）の Koin モジュール。
 *
 * 提供: NarratorOverlayViewModel と、それが使う ObserveLatestTelemetryLogUseCase。
 * 画面（Compose）は未実装。
 */
val narratorOverlayModule =
    module {
        viewModel { NarratorOverlayViewModel(get()) }

        factory { ObserveLatestTelemetryLogUseCase(get()) }
    }
