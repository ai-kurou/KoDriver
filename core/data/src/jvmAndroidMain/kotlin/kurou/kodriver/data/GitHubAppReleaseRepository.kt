package kurou.kodriver.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kurou.kodriver.domain.model.AppRelease
import kurou.kodriver.domain.repository.AppReleaseRepository
import java.net.HttpURLConnection
import java.net.URI

private const val LATEST_RELEASE_URL =
    "https://api.github.com/repos/ai-kurou/KoDriver/releases/latest"
private const val TIMEOUT_MS = 10_000

internal class GitHubAppReleaseRepository(
    private val latestReleaseUrl: String = LATEST_RELEASE_URL,
) : AppReleaseRepository {

    private val json = Json { ignoreUnknownKeys = true }

    @Suppress("UNENCRYPTED_SOCKET")
    override suspend fun getLatestRelease(): AppRelease? = withContext(Dispatchers.IO) {
        try {
            val connection = URI(latestReleaseUrl).toURL().openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

            val body = connection.inputStream.bufferedReader().readText()
            val tagName = json.parseToJsonElement(body).jsonObject["tag_name"]?.jsonPrimitive?.content
            tagName?.let { AppRelease(it) }
        } catch (_: Exception) {
            null
        }
    }
}
