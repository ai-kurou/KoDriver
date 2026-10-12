package kurou.kodriver.feature.otheroverlaytextcolordetail

import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextColorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * オーバーレイ文字色設定詳細（other-overlay-text-color-detail feature）の Koin モジュール。
 *
 * 提供: OtherOverlayTextColorDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: OverlayTextColorPreferencesRepository（:core:data で登録）。
 */
val otherOverlayTextColorDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherOverlayTextColorDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveOverlayTextColorUseCase(get()) }
        factory { SaveOverlayTextColorUseCase(get()) }
    }
