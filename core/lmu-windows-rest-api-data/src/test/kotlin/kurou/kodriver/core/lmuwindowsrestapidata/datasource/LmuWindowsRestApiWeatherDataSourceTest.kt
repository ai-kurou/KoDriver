package kurou.kodriver.core.lmuwindowsrestapidata.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LmuWindowsRestApiWeatherDataSourceTest {
    @Test
    fun `fetchWeatherForecastはGETメソッドで_restsessionsweatherへリクエストする`() =
        runTest {
            var capturedRequest: HttpRequestData? = null
            val client = fakeHttpClient("{}") { capturedRequest = it }
            val dataSource = LmuWindowsRestApiWeatherDataSource(client = client)

            dataSource.fetchWeatherForecast()

            assertEquals(HttpMethod.Get, capturedRequest?.method)
            assertEquals("/rest/sessions/weather", capturedRequest?.url?.encodedPath)
        }

    @Test
    fun `fetchWeatherForecastはRESTレスポンスをDTOへ変換して返す`() =
        runTest {
            val responseBody =
                """
                {
                  "RACE": {
                    "START": {
                      "WNV_SKY": { "currentValue": 0, "stringValue": "晴天" },
                      "WNV_TEMPERATURE": { "currentValue": 23, "stringValue": "23 °" },
                      "WNV_RAIN_CHANCE": { "currentValue": 0, "stringValue": "0%" },
                      "WNV_HUMIDITY": { "currentValue": 75, "stringValue": "75%" },
                      "WNV_WINDDIRECTION": { "currentValue": 1, "stringValue": "North East" },
                      "WNV_WINDSPEED": { "currentValue": 5, "stringValue": "18.0 kph" }
                    }
                  }
                }
                """.trimIndent()
            val client = fakeHttpClient(responseBody)
            val dataSource = LmuWindowsRestApiWeatherDataSource(client = client)

            val result = dataSource.fetchWeatherForecast()

            assertNull(result.practice)
            assertNull(result.qualify)
            assertEquals(
                23.0,
                result.race
                    ?.start
                    ?.temperature
                    ?.currentValue,
            )
            assertEquals(
                "18.0 kph",
                result.race
                    ?.start
                    ?.windSpeed
                    ?.stringValue,
            )
        }

    @Test
    fun `fetchWeatherForecastはセッション情報が空でも空のDTOを返す`() =
        runTest {
            val client = fakeHttpClient("{}")
            val dataSource = LmuWindowsRestApiWeatherDataSource(client = client)

            val result = dataSource.fetchWeatherForecast()

            assertNull(result.practice)
            assertNull(result.qualify)
            assertNull(result.race)
        }

    @Test
    fun `fetchWeatherForecastはHTTP500応答を空予報として扱わずServerResponseExceptionを投げる`() =
        runTest {
            val client = fakeHttpClient(responseBody = "{}", status = HttpStatusCode.InternalServerError)
            val dataSource = LmuWindowsRestApiWeatherDataSource(client = client)

            assertFailsWith<ServerResponseException> { dataSource.fetchWeatherForecast() }
        }

    private fun fakeHttpClient(
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (HttpRequestData) -> Unit = {},
    ): HttpClient {
        val mockEngine =
            MockEngine { request ->
                onRequest(request)
                respond(
                    content = responseBody,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
