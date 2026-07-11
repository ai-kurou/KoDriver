package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * アプリのメイン画面（main feature）の Koin モジュール。
 *
 * 提供: AppScreenViewModel・ConnectionBannerViewModel と、それらが使うドメイン UseCase。
 * 消費（get で解決）: AppUpdateRepository・ExitConfirmationEnabledRepository・
 *   KeepScreenOnEnabledRepository（:core:data で登録）。アプリバージョンはビルド生成値を直接渡す。
 * プラットフォーム別の登録は [mainPlatformModule]（expect/actual）に分離している。
 */
val mainModule = module {
    // ViewModel
    viewModel { AppScreenViewModel(get(), currentAppVersion(), get(), get(), get()) }
    viewModelOf(::ConnectionBannerViewModel)

    // ドメイン UseCase（:core:domain。get() は :core:data の Repository を解決）
    factory { CheckAppUpdateAvailableUseCase(get()) }
    factory { ObserveExitConfirmationEnabledUseCase(get()) }
    factory { ObserveKeepScreenOnEnabledUseCase(get()) }
    factory { SaveExitConfirmationEnabledUseCase(get()) }
    factory { SaveKeepScreenOnEnabledUseCase(get()) }
}

expect val mainPlatformModule: Module
