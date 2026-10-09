package kurou.kodriver.core.lmuwindowsrestapidata.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiRepairAndRefuelDataSource
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val BODY = """{"wearables":{"brakes":[0.036,0.035,0.032,0.031]}}"""

class LmuWindowsRestApiBrakeWearRepositoryTest {
    @Test
    fun `wearables_brakesを4輪分のブレーキ残り厚さとして流す`() =
        runTest {
            val repository = repository { plainText(BODY) }

            val result = repository.brakeWearStream().first()

            assertEquals(BrakeThicknessMeters(0.036f), result.wheels.getValue(WheelIndex.FRONT_LEFT))
            assertEquals(BrakeThicknessMeters(0.031f), result.wheels.getValue(WheelIndex.REAR_RIGHT))
        }

    @Test
    fun `取得に失敗した回は流さず次の周期で再取得する`() =
        runTest {
            var calls = 0
            val repository =
                repository {
                    calls++
                    if (calls == 1) respondError(HttpStatusCode.InternalServerError) else plainText(BODY)
                }

            val results = repository.brakeWearStream().take(1).toList()

            assertEquals(1, results.size)
            assertEquals(2, calls)
        }

    @Test
    fun `4輪分が揃っていない回は流さず次の周期で再取得する`() =
        runTest {
            var calls = 0
            val repository =
                repository {
                    calls++
                    if (calls == 1) plainText("""{"wearables":{"brakes":[0.036]}}""") else plainText(BODY)
                }

            val results = repository.brakeWearStream().take(1).toList()

            assertEquals(1, results.size)
            assertEquals(2, calls)
        }

    @Test
    fun `取得中のCancellationExceptionは握りつぶさず再スローする`() =
        runTest {
            val repository = repository { throw CancellationException("cancelled") }

            assertFailsWith<CancellationException> { repository.brakeWearStream().first() }
        }

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): LmuWindowsRestApiBrakeWearRepository {
        val client = HttpClient(MockEngine(handler)) { expectSuccess = true }
        return LmuWindowsRestApiBrakeWearRepository(
            dataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client),
            pollIntervalMillis = 1_000L,
        )
    }

    private fun MockRequestHandleScope.plainText(body: String): HttpResponseData =
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
        )
}
