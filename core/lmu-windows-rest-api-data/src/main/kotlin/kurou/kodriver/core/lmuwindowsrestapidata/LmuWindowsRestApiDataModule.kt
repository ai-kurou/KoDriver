package kurou.kodriver.core.lmuwindowsrestapidata

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiWeatherDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.repository.LmuWindowsWeatherForecastRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsWeatherForecastRepository
import org.koin.dsl.module

/**
 * LMU内蔵ローカルREST API（`http://localhost:6397`）の Repository バインドを行う Koin モジュール
 * （:core:lmu-windows-rest-api-data。JVM 専用）。
 */
val lmuWindowsRestApiDataModule =
    module {
        single {
            HttpClient(OkHttp) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
        single { LmuWindowsRestApiWeatherDataSource(client = get()) }
        single<LmuWindowsWeatherForecastRepository> { LmuWindowsWeatherForecastRepositoryImpl(dataSource = get()) }
    }
