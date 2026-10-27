package kurou.kodriver.core.gt7ps5data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpSource
import kurou.kodriver.core.gt7ps5data.preferences.createGt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.core.gt7ps5data.preferences.createGt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.core.gt7ps5data.preferences.createGt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.core.gt7ps5data.preferences.createGt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.core.gt7ps5data.repository.Gt7Ps5RepositoryImpl
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5Repository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5UdpPortPreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private const val GT7_PS5_SCOPE_QUALIFIER = "gt7_ps5_scope"

/**
 * GT7 PS5 テレメトリの Repository バインドを行う Koin モジュール（:core:gt7-ps5-data / androidMain）。
 *
 * app エントリーポイントで束ねられ、gt7-ps5-connection / gt7-ps5-narrator の UseCase が
 * get() で解決する Gt7Ps5Repository を提供する。UDP 受信は専用の CoroutineScope 上で行う。
 * jvmMain 版との違いは設定ファイルの保存先（context.filesDir）のみ。
 */
val gt7Ps5DataModule =
    module {
        single {
            Gt7Ps5AutoReadoutSettingsResetter(
                myBestLapPreferencesRepository = get(),
                remainingFuelLapsPreferencesRepository = get(),
                remainingFuelPreferencesRepository = get(),
                tyreTemperaturePreferencesRepository = get(),
            )
        } bind AutoReadoutSettingsResetter::class
        // UDP 受信を回す専用スコープ（named で他スコープと分離）
        single(named(GT7_PS5_SCOPE_QUALIFIER)) { CoroutineScope(SupervisorJob()) }

        // 設定永続化（DataStore）
        single<Gt7Ps5UdpPortPreferencesRepository> {
            createGt7Ps5UdpPortPreferencesRepository(get<Context>().filesDir.absolutePath)
        }

        // GT7 設定永続化（DataStore）
        single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
            createGt7Ps5RemainingFuelLapsPreferencesRepository(get<Context>().filesDir.absolutePath)
        }
        single<Gt7Ps5RemainingFuelPreferencesRepository> {
            createGt7Ps5RemainingFuelPreferencesRepository(get<Context>().filesDir.absolutePath)
        }
        single<Gt7Ps5TyreTemperaturePreferencesRepository> {
            createGt7Ps5TyreTemperaturePreferencesRepository(get<Context>().filesDir.absolutePath)
        }
        single<Gt7Ps5MyBestLapPreferencesRepository> {
            createGt7Ps5MyBestLapPreferencesRepository(get<Context>().filesDir.absolutePath)
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
