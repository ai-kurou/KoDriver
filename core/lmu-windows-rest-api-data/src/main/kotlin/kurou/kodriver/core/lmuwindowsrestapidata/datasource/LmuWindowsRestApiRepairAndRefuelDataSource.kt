package kurou.kodriver.core.lmuwindowsrestapidata.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json
import kurou.kodriver.core.lmuwindowsrestapidata.dto.RepairAndRefuelResponseDto

/**
 * `GET /rest/garage/UIScreen/RepairAndRefuel` を取得する。
 *
 * LMU は `Content-Type: text/plain`（charset なし）で返すため、ContentNegotiation に頼らず
 * ボディを文字列として読み取り、自前で JSON としてデコードする。
 */
internal class LmuWindowsRestApiRepairAndRefuelDataSource(
    private val client: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchRepairAndRefuel(): RepairAndRefuelResponseDto {
        val text = client.get("$baseUrl$REPAIR_AND_REFUEL_PATH").body<String>()
        return json.decodeFromString(text)
    }

    companion object {
        private const val DEFAULT_BASE_URL = "http://localhost:6397"
        private const val REPAIR_AND_REFUEL_PATH = "/rest/garage/UIScreen/RepairAndRefuel"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
