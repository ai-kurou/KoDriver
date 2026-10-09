package kurou.kodriver.core.lmuwindowsrestapidata

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiBrakeInfoDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiRepairAndRefuelDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.repository.LmuWindowsRestApiBrakeWearInvestigationRepository
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val REQUEST_TIMEOUT_MILLIS = 2_000L

/** LMU 内蔵 REST API 用の HttpClient を他の HttpClient と区別するための Koin 修飾子。 */
private val lmuRestApiHttpClient = named("lmuWindowsRestApiHttpClient")

/**
 * LMU 内蔵 REST API（`http://localhost:6397`）経由の Repository バインドを行う Koin モジュール
 * （:core:lmu-windows-rest-api-data。JVM 専用）。
 *
 * 提供: LmuWindowsBrakeWearInvestigationRepository（ブレーキ摩耗の実機調査用の生値ポーリング）。
 * REST API は LMU を起動した Windows 機の `localhost` にのみ待ち受けるため、デスクトップ版の
 * エントリーポイントでのみ束ねる。
 */
val lmuWindowsRestApiDataModule =
    module {
        single(lmuRestApiHttpClient) {
            HttpClient(OkHttp) {
                install(HttpTimeout) {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                }
            }
        }
        single<LmuWindowsBrakeWearInvestigationRepository> {
            val client = get<HttpClient>(lmuRestApiHttpClient)
            LmuWindowsRestApiBrakeWearInvestigationRepository(
                repairAndRefuelDataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client),
                brakeInfoDataSource = LmuWindowsRestApiBrakeInfoDataSource(client),
            )
        }
    }
