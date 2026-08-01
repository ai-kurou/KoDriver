package kurou.kodriver.feature.gt7ps5connection

import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * GT7 PS5 接続状態監視（gt7-ps5-connection feature）の Koin モジュール。
 *
 * 提供: Gt7Ps5ConnectionViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: Gt7Ps5Repository（:core:gt7-ps5-data）・SimulatorPreferencesRepository（:core:data）。
 */
val gt7Ps5ConnectionModule =
    module {
        // ViewModel
        viewModelOf(::Gt7Ps5ConnectionViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:gt7-ps5-data / :core:data の Repository を解決）
        factory { CheckGt7Ps5ConnectionUseCase(get()) }
        factory { ObserveGt7Ps5UseCase(get()) }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { ObserveGt7Ps5ConnectionUseCase(get(), get()) }
    }
