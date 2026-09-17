package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * オーバーレイ表示機能（narrator-overlay feature）の Koin モジュール。
 *
 * 提供: NarratorOverlayViewModel と、それが使う ObserveLatestTelemetryLogUseCase・ObserveOverlayTextSizeUseCase。
 */
val narratorOverlayModule =
    module {
        viewModel { NarratorOverlayViewModel(get(), get()) }

        factory { ObserveLatestTelemetryLogUseCase(get()) }
        factory { ObserveOverlayTextSizeUseCase(get()) }
    }
