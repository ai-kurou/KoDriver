package kurou.kodriver.core.lmuwindowsrestapidata.datasource

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LmuWindowsRestApiRepairAndRefuelDataSourceTest {
    @Test
    fun `fetchRepairAndRefuelはGETメソッドで_restgarageUIScreenRepairAndRefuelへリクエストする`() =
        runTest {
            var capturedRequest: HttpRequestData? = null
            val client = fakeHttpClient("{}") { capturedRequest = it }
            val dataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client = client)

            dataSource.fetchRepairAndRefuel()

            assertEquals(HttpMethod.Get, capturedRequest?.method)
            assertEquals("/rest/garage/UIScreen/RepairAndRefuel", capturedRequest?.url?.encodedPath)
        }

    @Test
    fun `fetchRepairAndRefuelはtextplainで返るレスポンスからブレーキ厚さを読み取り未知のキーは無視する`() =
        runTest {
            val responseBody =
                """
                {
                  "fuelInfo": { "currentFuel": 12.5 },
                  "wearables": {
                    "body": { "aero": 0.0 },
                    "brakes": [0.036, 0.035, 0.032, 0.031],
                    "tires": [1.0, 1.0, 1.0, 1.0]
                  }
                }
                """.trimIndent()
            val client = fakeHttpClient(responseBody)
            val dataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client = client)

            val result = dataSource.fetchRepairAndRefuel()

            assertEquals(listOf(0.036, 0.035, 0.032, 0.031), result.wearables?.brakes)
        }

    @Test
    fun `fetchRepairAndRefuelはwearablesが無くても空のDTOを返す`() =
        runTest {
            val client = fakeHttpClient("{}")
            val dataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client = client)

            val result = dataSource.fetchRepairAndRefuel()

            assertNull(result.wearables)
        }

    @Test
    fun `fetchRepairAndRefuelはHTTP500応答でServerResponseExceptionを投げる`() =
        runTest {
            val client = fakeHttpClient(responseBody = "{}", status = HttpStatusCode.InternalServerError)
            val dataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client = client)

            assertFailsWith<ServerResponseException> { dataSource.fetchRepairAndRefuel() }
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
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            }
        return HttpClient(mockEngine) { expectSuccess = true }
    }
}
