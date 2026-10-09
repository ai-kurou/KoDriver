package kurou.kodriver.core.lmuwindowsrestapidata.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

/**
 * `GET /rest/garage/brakeinfo`（4 輪分の数値配列）を取得する。
 *
 * [LmuWindowsRestApiRepairAndRefuelDataSource] と同様、`Content-Type: text/plain` で返るため
 * ボディを文字列として読み取り、自前で JSON としてデコードする。
 */
internal class LmuWindowsRestApiBrakeInfoDataSource(
    private val client: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchBrakeInfo(): List<Double> {
        val text =
            client
                .get("$baseUrl$BRAKE_INFO_PATH") {
                    expectSuccess = true
                }.body<String>()
        return json.decodeFromString(text)
    }

    companion object {
        private const val DEFAULT_BASE_URL = "http://localhost:6397"
        private const val BRAKE_INFO_PATH = "/rest/garage/brakeinfo"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
