package kurou.kodriver.core.serverdiscoverydata

import kurou.kodriver.domain.repository.ServerConnectivityRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * LAN 内の KoDriver サーバーに関する Repository のバインドを行う Koin モジュール（:core:server-discovery-data）。
 *
 * 提供: ServerConnectivityRepository。
 */
public val serverDiscoveryDataModule: Module =
    module {
        factory<ServerConnectivityRepository> { TcpServerConnectivityRepository() }
    }
