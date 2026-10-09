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

class LmuWindowsRestApiBrakeInfoDataSourceTest {
    @Test
    fun `fetchBrakeInfoはGETメソッドで_restgaragebrakeinfoへリクエストする`() =
        runTest {
            var capturedRequest: HttpRequestData? = null
            val client = fakeHttpClient("[0.036,0.036,0.032,0.032]") { capturedRequest = it }
            val dataSource = LmuWindowsRestApiBrakeInfoDataSource(client = client)

            dataSource.fetchBrakeInfo()

            assertEquals(HttpMethod.Get, capturedRequest?.method)
            assertEquals("/rest/garage/brakeinfo", capturedRequest?.url?.encodedPath)
        }

    @Test
    fun `fetchBrakeInfoはtextplainで返る数値配列を読み取る`() =
        runTest {
            val client = fakeHttpClient("[0.036, 0.036, 0.032, 0.032]")
            val dataSource = LmuWindowsRestApiBrakeInfoDataSource(client = client)

            assertEquals(listOf(0.036, 0.036, 0.032, 0.032), dataSource.fetchBrakeInfo())
        }

    @Test
    fun `fetchBrakeInfoはHTTP500応答でServerResponseExceptionを投げる`() =
        runTest {
            val dataSource =
                LmuWindowsRestApiBrakeInfoDataSource(
                    client = fakeHttpClient("[]", status = HttpStatusCode.InternalServerError, expectSuccess = false),
                )

            assertFailsWith<ServerResponseException> { dataSource.fetchBrakeInfo() }
        }

    private fun fakeHttpClient(
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        expectSuccess: Boolean = true,
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
        return HttpClient(mockEngine) { this.expectSuccess = expectSuccess }
    }
}
