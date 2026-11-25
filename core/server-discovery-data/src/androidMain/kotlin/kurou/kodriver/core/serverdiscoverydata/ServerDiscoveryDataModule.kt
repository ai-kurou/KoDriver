package kurou.kodriver.core.serverdiscoverydata

import android.content.Context
import kurou.kodriver.domain.repository.ServerConnectivityRepository
import kurou.kodriver.domain.repository.WindowsServerDiscoveryRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * LAN 内の KoDriver サーバーに関する Repository のバインドを行う Koin モジュール（:core:server-discovery-data）。
 *
 * 提供: ServerConnectivityRepository、WindowsServerDiscoveryRepository。
 * 消費（get で解決）: Context。
 */
public val serverDiscoveryDataModule: Module =
    module {
        factory<ServerConnectivityRepository> { TcpServerConnectivityRepository() }
        factory<WindowsServerDiscoveryRepository> { NsdWindowsServerDiscoveryRepository(get<Context>()) }
    }
