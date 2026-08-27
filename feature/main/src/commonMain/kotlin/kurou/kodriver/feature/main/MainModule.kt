package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveEffectiveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryReceivingUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * アプリのメイン画面（main feature）の Koin モジュール。
 *
 * 提供: AppScreenViewModel・ConnectionBannerViewModel と、それらが使うドメイン UseCase。
 * 消費（get で解決）: AppUpdateRepository・KeepScreenOnEnabledRepository・
 *   SimulatorPreferencesRepository（:core:data で登録）。アプリバージョンはビルド生成値を直接渡す。
 * プラットフォーム別の登録は [mainPlatformModule]（expect/actual）に分離している。
 */
val mainModule =
    module {
        // ViewModel
        viewModel { AppScreenViewModel(get(), currentAppVersion(), get(), get(), get(), get(), get()) }
        viewModelOf(::ConnectionBannerViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Repository を解決）
        factory { CheckAppUpdateAvailableUseCase(get()) }
        factory { ObserveKeepScreenOnEnabledUseCase(get()) }
        factory { SaveKeepScreenOnEnabledUseCase(get()) }
        factory { ObserveDynamicColorEnabledUseCase(get()) }
        factory { SaveDynamicColorEnabledUseCase(get()) }
        factory { ObserveHapticFeedbackEnabledUseCase(get()) }
        factory { SaveHapticFeedbackEnabledUseCase(get()) }
        factory { CheckHapticFeedbackAvailableUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { SaveSelectedSimulatorUseCase(get()) }
        factory { ObserveLmuWindowsUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveAceWindowsStatusUseCase(get()) }
        factory { ObserveTelemetryReceivingUseCase(get(), get(), get(), get()) }
        factory { ObserveEffectiveKeepScreenOnUseCase(get(), get()) }
    }

expect val mainPlatformModule: Module
