package kurou.kodriver.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kurou.kodriver.domain.repository.ServerVersionRepository

private const val DEFAULT_PORT = 8080
private const val PATH = "/version"

internal class HttpServerVersionRepository(
    private val port: Int = DEFAULT_PORT,
) : ServerVersionRepository {

    private val client = HttpClient(OkHttp)

    @SuppressWarnings("SSRF")
    override suspend fun fetchVersion(ip: String): Result<String> {
        return try {
            val response = client.get("http://$ip:$port$PATH")
            if (response.status.isSuccess()) {
                val body = response.bodyAsText()
                val version = parseVersion(body)
                    ?: return Result.failure(IllegalStateException("unexpected response: $body"))
                Result.success(version)
            } else {
                Result.failure(IllegalStateException("HTTP ${response.status.value}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseVersion(json: String): String? {
        val match = Regex(""""version"\s*:\s*"([^"]+)"""").find(json)
        return match?.groupValues?.getOrNull(1)
    }
}
