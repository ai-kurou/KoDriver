package kurou.kodriver.feature.narratoroverlay

import kurou.kodriver.domain.overlay.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.overlay.usecase.ObserveOverlayWindowBoundsUseCase
import kurou.kodriver.domain.overlay.usecase.SaveOverlayWindowBoundsUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ObserveSpeechRecognitionTranscriptUseCase
import kurou.kodriver.domain.telemetrylog.usecase.ObserveLatestNarratedTelemetryLogUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * オーバーレイ表示機能（narrator-overlay feature）の Koin モジュール。
 *
 * 提供: NarratorOverlayViewModel と、それが使う ObserveLatestNarratedTelemetryLogUseCase・
 * ObserveOverlayTextSizeUseCase・ObserveOverlayTextColorUseCase・ObserveOverlayBackgroundOpacityUseCase・
 * ObserveSpeechRecognitionTranscriptUseCase。加えて、
 * rememberNarratorOverlayVisible が使う ObserveOverlayVisibleUseCase、rememberNarratorOverlayBounds /
 * rememberNarratorOverlayBoundsSaver が使う ObserveOverlayWindowBoundsUseCase・SaveOverlayWindowBoundsUseCase。
 */
public val narratorOverlayModule: Module =
    module {
        viewModel { NarratorOverlayViewModel(get(), get(), get(), get(), get()) }

        factory { ObserveLatestNarratedTelemetryLogUseCase(get()) }
        factory { ObserveSpeechRecognitionTranscriptUseCase(get()) }
        factory { ObserveOverlayTextColorUseCase(get()) }
        factory { ObserveOverlayTextSizeUseCase(get()) }
        factory { ObserveOverlayBackgroundOpacityUseCase(get()) }
        factory { ObserveOverlayVisibleUseCase(get()) }
        factory { ObserveOverlayWindowBoundsUseCase(get()) }
        factory { SaveOverlayWindowBoundsUseCase(get()) }
    }
