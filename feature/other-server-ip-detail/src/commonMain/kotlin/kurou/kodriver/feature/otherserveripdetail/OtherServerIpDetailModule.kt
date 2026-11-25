package kurou.kodriver.feature.otherserveripdetail

import kurou.kodriver.domain.usecase.CheckServerConnectivityUseCase
import kurou.kodriver.domain.usecase.DiscoverWindowsServersUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 接続先サーバー IP 設定ダイアログ（other-server-ip-detail feature）の Koin モジュール。
 *
 * 提供: OtherServerIpDetailViewModel と、それが使うドメイン UseCase。
 * 消費（get で解決）: ServerIpPreferencesRepository（:core:data で登録）、
 *   ServerConnectivityRepository / WindowsServerDiscoveryRepository
 *   （:core:server-discovery-data で登録）。
 */
public val otherServerIpDetailModule: Module =
    module {
        // ViewModel
        viewModelOf(::OtherServerIpDetailViewModel)

        // ドメイン UseCase（:core:domain。get() は :core:data の Preferences Repository を解決）
        factory { ObserveServerIpUseCase(get()) }
        factory { SaveServerIpUseCase(get()) }
        // ServerConnectivityRepository は :core:server-discovery-data で登録する
        factory { CheckServerConnectivityUseCase(get()) }
        // WindowsServerDiscoveryRepository は :core:server-discovery-data で登録する
        factory { DiscoverWindowsServersUseCase(get()) }
        factory { ValidateServerIpAddressUseCase() }
        factory { SaveServerIpWithConnectivityCheckUseCase(get(), get(), get()) }
    }
