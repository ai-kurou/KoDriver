package kurou.kodriver.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitHubAppReleaseRepositoryTest {
    @Test
    fun `tag_nameを含むJSONのときAppUpdateとして返す`() =
        runTest {
            val repository =
                GitHubAppReleaseRepository(
                    fetch = { """{"tag_name":"v1.2.3","name":"Release 1.2.3"}""" },
                )

            val update = repository.getLatestRelease()

            assertEquals("v1.2.3", update?.tagName)
        }

    @Test
    fun `fetchがnullを返すときnullを返す`() =
        runTest {
            val repository = GitHubAppReleaseRepository(fetch = { null })

            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `tag_nameが含まれないJSONのときnullを返す`() =
        runTest {
            val repository =
                GitHubAppReleaseRepository(
                    fetch = { """{"message":"Not Found"}""" },
                )

            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `fetchが例外をスローするときnullを返す`() =
        runTest {
            val repository = GitHubAppReleaseRepository(fetch = { error("network error") })

            assertNull(repository.getLatestRelease())
        }

    @Test
    fun `fetchがCancellationExceptionをスローするとき再スローする`() =
        runTest {
            val repository = GitHubAppReleaseRepository(fetch = { throw CancellationException() })

            assertFailsWith<CancellationException> { repository.getLatestRelease() }
        }

    @Test
    fun `GitHubの最新リリースURLは許可する`() {
        val uri = URI("https://api.github.com/repos/ai-kurou/KoDriver/releases/latest")

        assertTrue(uri.isAllowedGitHubLatestReleaseUri())
    }

    @Test
    fun `GitHubの最新リリースURL以外は許可しない`() {
        val uris =
            listOf(
                URI("http://api.github.com/repos/ai-kurou/KoDriver/releases/latest"),
                URI("https://example.com/repos/ai-kurou/KoDriver/releases/latest"),
                URI("https://api.github.com/repos/ai-kurou/KoDriver/releases/latest?redirect=https://example.com"),
                URI("https://api.github.com/repos/ai-kurou/KoDriver/releases/latest#fragment"),
            )

        uris.forEach { uri ->
            assertFalse(uri.isAllowedGitHubLatestReleaseUri())
        }
    }
}
