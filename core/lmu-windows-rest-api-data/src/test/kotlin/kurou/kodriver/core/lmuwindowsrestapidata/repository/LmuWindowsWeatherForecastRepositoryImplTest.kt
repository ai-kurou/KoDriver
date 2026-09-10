package kurou.kodriver.core.lmuwindowsrestapidata.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiWeatherDataSource
import kurou.kodriver.domain.model.LmuWindowsWeatherSessionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsWeatherForecastRepositoryImplTest {
    @Test
    fun `weatherForecastsはデータソースの取得結果をドメインモデルへ変換して返す`() =
        runTest {
            val responseBody =
                """
                {
                  "RACE": {
                    "START": {
                      "WNV_TEMPERATURE": { "currentValue": 23, "stringValue": "23 °" }
                    }
                  }
                }
                """.trimIndent()
            val repository = LmuWindowsWeatherForecastRepositoryImpl(dataSource = fakeDataSource(responseBody))

            val result = repository.weatherForecasts()

            assertEquals(listOf(LmuWindowsWeatherSessionType.RACE), result.map { it.sessionType })
            assertEquals(
                23,
                result
                    .single()
                    .nodes
                    .single()
                    .temperatureCelsius,
            )
        }

    @Test
    fun `weatherForecastsはセッション情報が無い場合は空リストを返す`() =
        runTest {
            val repository = LmuWindowsWeatherForecastRepositoryImpl(dataSource = fakeDataSource("{}"))

            val result = repository.weatherForecasts()

            assertTrue(result.isEmpty())
        }

    private fun fakeDataSource(responseBody: String): LmuWindowsRestApiWeatherDataSource {
        val mockEngine =
            MockEngine { _ ->
                respond(
                    content = responseBody,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val client =
            HttpClient(mockEngine) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        return LmuWindowsRestApiWeatherDataSource(client = client)
    }
}
