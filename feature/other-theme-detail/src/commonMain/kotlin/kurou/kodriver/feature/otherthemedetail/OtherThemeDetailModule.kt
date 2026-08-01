package kurou.kodriver.feature.otherthemedetail

import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveThemeModeUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * テーマ設定詳細（other-theme-detail feature）の Koin モジュール。
 *
 * 提供: OtherThemeDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: ThemePreferencesRepository（:core:data で登録）。
 */
val otherThemeDetailModule =
    module {
    // ViewModel
    viewModelOf(::OtherThemeDetailViewModel)

    // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
    factory { ObserveThemeModeUseCase(get()) }
    factory { SaveThemeModeUseCase(get()) }
}
