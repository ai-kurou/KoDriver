package kurou.kodriver.core.serverdiscoverydata

import kurou.kodriver.domain.serverconnection.repository.ServerConnectivityRepository
import kurou.kodriver.domain.serverconnection.repository.WindowsServerDiscoveryRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * LAN 内の KoDriver サーバーに関する Repository のバインドを行う Koin モジュール（:core:server-discovery-data）。
 *
 * 提供: ServerConnectivityRepository、WindowsServerDiscoveryRepository。
 */
public val serverDiscoveryDataModule: Module =
    module {
        factory<ServerConnectivityRepository> { TcpServerConnectivityRepository() }
        factory<WindowsServerDiscoveryRepository> { JmdnsWindowsServerDiscoveryRepository() }
    }
