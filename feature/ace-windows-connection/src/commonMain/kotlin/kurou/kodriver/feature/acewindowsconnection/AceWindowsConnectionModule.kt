package kurou.kodriver.feature.acewindowsconnection

import kurou.kodriver.domain.usecase.CheckAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ACE (Assetto Corsa EVO) Windows版 接続状態監視（ace-windows-connection feature）の Koin モジュール。
 *
 * 提供: AceWindowsConnectionViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: AceWindowsFuelRepository（:core:ace-windows-data / :core:data）・
 *   SimulatorPreferencesRepository（:core:data）。
 */
val aceWindowsConnectionModule =
    module {
        // ViewModel
        viewModelOf(::AceWindowsConnectionViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:ace-windows-data / :core:data の Repository を解決）
        factory { CheckAceWindowsConnectionUseCase(get()) }
        factory { ObserveAceWindowsFuelUseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveAceWindowsConnectionUseCase(get(), get()) }
    }
