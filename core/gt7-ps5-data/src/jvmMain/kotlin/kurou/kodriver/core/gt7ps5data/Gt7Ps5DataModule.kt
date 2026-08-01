package kurou.kodriver.core.gt7ps5data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

/**
 * GT7 PS5 テレメトリの Repository バインドを行う Koin モジュール（:core:gt7-ps5-data / jvmMain）。
 *
 * app エントリーポイントで束ねられ、gt7-ps5-connection / gt7-ps5-narrator の UseCase が
 * get() で解決する Gt7Ps5Repository を提供する。UDP 受信は専用の CoroutineScope 上で行う。
 * Android 版は androidMain の同名モジュールを参照。
 */
val gt7Ps5DataModule =
    module {
        // UDP 受信を回す専用スコープ（named で他スコープと分離）
        single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }

        // 設定永続化（DataStore）
        single<Gt7Ps5UdpPortPreferencesRepository> {
            createGt7Ps5UdpPortPreferencesRepository("${System.getProperty("user.home")}/.kodriver")
        }

        // データソース・Repository（UDP パケット受信。get() で接続先アドレス・待受ポート設定を解決）
        single<Gt7Ps5PacketSource> {
            Gt7Ps5UdpSource(
                consoleAddressFlow = get<ConsoleAddressPreferencesRepository>().consoleAddress(),
                listenPortFlow = get<Gt7Ps5UdpPortPreferencesRepository>().port(),
                scope = get(named(GT7_PS5_SCOPE_QUALIFIER)),
            )
        }
        single<Gt7Ps5Repository> { Gt7Ps5RepositoryImpl(source = get()) }
    }
