package kurou.kodriver.feature.otheroverlaytextsizedetail

import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextSizeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * オーバーレイ文字サイズ設定詳細（other-overlay-text-size-detail feature）の Koin モジュール。
 *
 * 提供: OtherOverlayTextSizeDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: OverlayTextSizePreferencesRepository（:core:data で登録）。
 */
val otherOverlayTextSizeDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherOverlayTextSizeDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveOverlayTextSizeUseCase(get()) }
        factory { SaveOverlayTextSizeUseCase(get()) }
    }
