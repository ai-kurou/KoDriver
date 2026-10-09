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
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiBrakeInfoDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiRepairAndRefuelDataSource
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val REPAIR_AND_REFUEL_PATH = "/rest/garage/UIScreen/RepairAndRefuel"
private const val BRAKE_INFO_PATH = "/rest/garage/brakeinfo"
private const val REPAIR_AND_REFUEL_BODY = """{"wearables":{"brakes":[0.036,0.035,0.032,0.031]}}"""
private const val BRAKE_INFO_BODY = "[0.036,0.036,0.032,0.032]"

private val WEARABLES_BRAKES = listOf(0.036, 0.035, 0.032, 0.031)
private val BRAKE_INFO = listOf(0.036, 0.036, 0.032, 0.032)

class LmuWindowsRestApiBrakeWearInvestigationRepositoryTest {
    @Test
    fun `両エンドポイントの生の配列値をそのまま流す`() =
        runTest {
            val repository = repository { normalResponse(it) }

            val result = repository.investigationStream().first()

            assertEquals(LmuWindowsBrakeWearInvestigationData(WEARABLES_BRAKES, BRAKE_INFO), result)
        }

    @Test
    fun `brakeinfoの取得に失敗してもwearablesは流しbrakeInfoはnullになる`() =
        runTest {
            val repository =
                repository {
                    if (it.url.encodedPath == BRAKE_INFO_PATH) {
                        respondError(HttpStatusCode.InternalServerError)
                    } else {
                        normalResponse(it)
                    }
                }

            val result = repository.investigationStream().first()

            assertEquals(LmuWindowsBrakeWearInvestigationData(WEARABLES_BRAKES, brakeInfo = null), result)
        }

    @Test
    fun `wearablesの取得に失敗してもbrakeinfoは流しwearablesBrakesはnullになる`() =
        runTest {
            val repository =
                repository {
                    if (it.url.encodedPath == REPAIR_AND_REFUEL_PATH) {
                        respondError(HttpStatusCode.InternalServerError)
                    } else {
                        normalResponse(it)
                    }
                }

            val result = repository.investigationStream().first()

            assertEquals(LmuWindowsBrakeWearInvestigationData(wearablesBrakes = null, brakeInfo = BRAKE_INFO), result)
        }

    @Test
    fun `wearablesが含まれないレスポンスではwearablesBrakesはnullになる`() =
        runTest {
            val repository =
                repository {
                    if (it.url.encodedPath == REPAIR_AND_REFUEL_PATH) plainText("{}") else normalResponse(it)
                }

            val result = repository.investigationStream().first()

            assertNull(result.wearablesBrakes)
            assertEquals(BRAKE_INFO, result.brakeInfo)
        }

    @Test
    fun `ポーリングのたびに両エンドポイントへ再リクエストする`() =
        runTest {
            val requestedPaths = mutableListOf<String>()
            val repository =
                repository {
                    requestedPaths += it.url.encodedPath
                    normalResponse(it)
                }

            repository.investigationStream().take(2).toList()

            assertEquals(
                listOf(REPAIR_AND_REFUEL_PATH, BRAKE_INFO_PATH, REPAIR_AND_REFUEL_PATH, BRAKE_INFO_PATH),
                requestedPaths,
            )
        }

    @Test
    fun `取得中のCancellationExceptionは握りつぶさず再スローする`() =
        runTest {
            val repository = repository { throw CancellationException("cancelled") }

            assertFailsWith<CancellationException> { repository.investigationStream().first() }
        }

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): LmuWindowsRestApiBrakeWearInvestigationRepository {
        val client = HttpClient(MockEngine(handler)) { expectSuccess = true }
        return LmuWindowsRestApiBrakeWearInvestigationRepository(
            repairAndRefuelDataSource = LmuWindowsRestApiRepairAndRefuelDataSource(client),
            brakeInfoDataSource = LmuWindowsRestApiBrakeInfoDataSource(client),
            pollIntervalMillis = 1_000L,
        )
    }

    private fun MockRequestHandleScope.normalResponse(request: HttpRequestData): HttpResponseData =
        plainText(if (request.url.encodedPath == REPAIR_AND_REFUEL_PATH) REPAIR_AND_REFUEL_BODY else BRAKE_INFO_BODY)

    private fun MockRequestHandleScope.plainText(body: String): HttpResponseData =
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
        )
}
