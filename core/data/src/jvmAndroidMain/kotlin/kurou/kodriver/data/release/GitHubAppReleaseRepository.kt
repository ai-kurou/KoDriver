package kurou.kodriver.data.release

import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
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

    override suspend fun getLatestRelease(): AppUpdate? =
        withContext(Dispatchers.IO) {
            try {
                val body = fetch() ?: return@withContext null
                val tagName =
                    json
                        .parseToJsonElement(body)
                        .jsonObject["tag_name"]
                        ?.jsonPrimitive
                        ?.content
                tagName?.let { AppUpdate(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Sentry.captureException(e)
                null
            }
        }
}

@Suppress("UNENCRYPTED_SOCKET")
private fun fetchLatestReleaseBody(): String? {
    return try {
        val releaseUri = URI(LATEST_RELEASE_URL)
        if (!releaseUri.isAllowedGitHubLatestReleaseUri()) return null
        val connection = releaseUri.toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
        connection.inputStream.bufferedReader().readText()
    } catch (e: Exception) {
        Sentry.captureException(e)
        null
    }
}

internal fun URI.isAllowedGitHubLatestReleaseUri(): Boolean =
    scheme == "https" &&
        host == "api.github.com" &&
        rawPath == "/repos/ai-kurou/KoDriver/releases/latest" &&
        rawQuery == null &&
        rawFragment == null
