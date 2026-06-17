package kurou.kodriver.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import java.net.HttpURLConnection
import java.net.URI

private const val LATEST_RELEASE_URL =
    "https://api.github.com/repos/ai-kurou/KoDriver/releases/latest"
private const val TIMEOUT_MS = 10_000

internal class GitHubAppReleaseRepository(
    private val fetch: suspend () -> String? = ::fetchLatestReleaseBody,
) : AppUpdateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLatestRelease(): AppUpdate? = withContext(Dispatchers.IO) {
        try {
            val body = fetch() ?: return@withContext null
            val tagName = json.parseToJsonElement(body).jsonObject["tag_name"]?.jsonPrimitive?.content
            tagName?.let { AppUpdate(it) }
        } catch (_: Exception) {
            null
        }
    }
}

@Suppress("UNENCRYPTED_SOCKET")
private fun fetchLatestReleaseBody(): String? {
    return try {
        val connection = URI(LATEST_RELEASE_URL).toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
        connection.inputStream.bufferedReader().readText()
    } catch (_: Exception) {
        null
    }
}
