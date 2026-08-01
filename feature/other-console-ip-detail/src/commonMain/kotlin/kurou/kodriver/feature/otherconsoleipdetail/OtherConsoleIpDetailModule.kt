package kurou.kodriver.feature.otherconsoleipdetail

import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5UdpPortUseCase
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * ゲーム機 IP 設定ダイアログ（other-console-ip-detail feature）の Koin モジュール。
 *
 * 提供: OtherConsoleIpDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: ConsoleAddressPreferencesRepository・Gt7Ps5UdpPortPreferencesRepository
 *   （いずれも :core:data で登録）。
 */
val otherConsoleIpDetailModule =
    module {
        // ViewModel
        viewModelOf(::OtherConsoleIpDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveConsoleAddressUseCase(get()) }
        factory { SaveConsoleAddressUseCase(get()) }
        factory { ObserveGt7Ps5UdpPortUseCase(get()) }
        factory { SaveGt7Ps5UdpPortUseCase(get()) }
    }
