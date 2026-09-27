package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.SaveOverlayBackgroundOpacityUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * オーバーレイ背景透明度設定詳細（other-overlay-background-opacity-detail feature）の Koin モジュール。
 *
 * 提供: OtherOverlayBackgroundOpacityDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: OverlayBackgroundOpacityPreferencesRepository（:core:data で登録）。
 */
val otherOverlayBackgroundOpacityDetailModule =
    module {
        viewModelOf(::OtherOverlayBackgroundOpacityDetailViewModel)

        factory { ObserveOverlayBackgroundOpacityUseCase(get()) }
        factory { SaveOverlayBackgroundOpacityUseCase(get()) }
    }
