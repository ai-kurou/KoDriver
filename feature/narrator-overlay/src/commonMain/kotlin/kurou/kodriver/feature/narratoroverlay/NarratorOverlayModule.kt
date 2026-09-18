package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.usecase.ObserveLatestTelemetryLogUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * オーバーレイ表示機能（narrator-overlay feature）の Koin モジュール。
 *
 * 提供: NarratorOverlayViewModel と、それが使う ObserveLatestTelemetryLogUseCase・
 * ObserveOverlayTextSizeUseCase・ObserveOverlayBackgroundOpacityUseCase。加えて、
 * rememberNarratorOverlayVisible が使う ObserveOverlayVisibleUseCase。
 */
val narratorOverlayModule =
    module {
        viewModel { NarratorOverlayViewModel(get(), get(), get()) }

        factory { ObserveLatestTelemetryLogUseCase(get()) }
        factory { ObserveOverlayTextSizeUseCase(get()) }
        factory { ObserveOverlayBackgroundOpacityUseCase(get()) }
        factory { ObserveOverlayVisibleUseCase(get()) }
    }
