package kurou.kodriver

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.core.devicevolumedata.deviceVolumeDataModule
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.windowsstartupdata.windowsStartupDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.presentation.featureModules
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.check.checkKoinModules

/**
 * Main.kt の composition root（desktopDataModule + 各 :core:*data モジュール + featureModules +
 * appVersion 定数）と同一のモジュール構成を、Koin の `checkKoinModules()` で実際に解決してみることで検証する。
 *
 * 登録漏れ・型不一致・依存解決不能な構成があっても、これまでは実際にその画面/機能を操作するまで
 * 実行時エラーとして顕在化しなかった（#1445）。このテストは Main.kt が束ねるモジュール一覧が変わった
 * ときに追随して更新すること。
 *
 * 以下の実体はダミー実装で置き換える（`AppTest` と同じ方針。理由は各項目のコメントを参照）。
 * - `:core:lmu-windows-data` の `lmuWindowsDataModule` → `fakeLmuWindowsNarratorModule`（`AppTest` でも使用）。
 * - `:core:ace-windows-data` の `aceWindowsDataModule` → このファイル内のダミー実装。
 *   どちらも共有メモリ読み取り用の JNA（kernel32）バインディングが、コンストラクタのデフォルト引数
 *   評価時に `isWindows` 判定より先にネイティブライブラリのロードを試みるため、Windows 以外の環境
 *   （macOS/Linuxでの単体テスト実行環境含む）では `UnsatisfiedLinkError` になる。
 * - `ServerIpPreferencesRepository`・`ServerVersionRepository` はダミー実装で補う。
 *   `feature:other-server-ip-detail`・`feature:server-connection` は Android から Desktop 版サーバーへ
 *   接続する画面用のモジュールで、`OtherListItems.nonAndroid.kt` が Desktop では一覧から隠しており
 *   実際には get() で解決されない。実体は Android 版の `AndroidDataModule` にのみ登録される
 *   （意図的な非対称）。featureModules はプラットフォーム間で共有されるため Koin モジュール自体は
 *   Desktop 構成にも含まれ、グラフ検証のためにここで補う。
 */
class DesktopKoinModuleGraphTest {
    @Test
    fun `Desktop構成のKoinモジュールグラフが解決可能である`() {
        checkKoinModules(
            listOf(
                desktopDataModule,
                gt7Ps5DataModule,
                deviceVolumeDataModule,
                windowsStartupDataModule,
            ) +
                featureModules +
                listOf(
                    fakeLmuWindowsNarratorModule,
                    module {
                        single(named("appVersion")) { "test" }
                        single<ServerIpPreferencesRepository> {
                            object : ServerIpPreferencesRepository {
                                override fun serverIp() = flowOf<String?>(null)

                                override suspend fun saveServerIp(ip: String) = Unit
                            }
                        }
                        single<ServerVersionRepository> {
                            object : ServerVersionRepository {
                                override suspend fun fetchVersion(ip: String) = Result.success("test")
                            }
                        }
                        single<AceWindowsFuelRepository> {
                            object : AceWindowsFuelRepository {
                                override fun fuelStream() = emptyFlow<AceWindowsFuelData>()

                                override suspend fun isConnected() = false
                            }
                        }
                        single<AceWindowsFlagRepository> {
                            object : AceWindowsFlagRepository {
                                override fun flagStream() = emptyFlow<AceWindowsFlagData>()
                            }
                        }
                        single<AceWindowsStatusRepository> {
                            object : AceWindowsStatusRepository {
                                override fun statusStream() = emptyFlow<AceWindowsStatusData>()
                            }
                        }
                        single<AceWindowsBestLapTimeRepository> {
                            object : AceWindowsBestLapTimeRepository {
                                override fun bestLapTimeStream() = emptyFlow<AceWindowsBestLapTimeData>()
                            }
                        }
                        single<AceWindowsTyreCarcassTemperatureRepository> {
                            object : AceWindowsTyreCarcassTemperatureRepository {
                                override fun tyreCarcassTemperatureStream() =
                                    emptyFlow<AceWindowsTyreCarcassTemperatureData>()
                            }
                        }
                        single<AceWindowsVehicleApproachRepository> {
                            object : AceWindowsVehicleApproachRepository {
                                override fun vehicleApproachStream() = emptyFlow<AceWindowsVehicleApproachData>()
                            }
                        }
                    },
                ),
        )
    }
}
