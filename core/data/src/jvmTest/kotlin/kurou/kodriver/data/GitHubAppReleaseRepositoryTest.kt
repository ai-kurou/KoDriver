package kurou.kodriver.data

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubAppReleaseRepositoryTest {

    private val server = MockWebServer()

    @BeforeTest
    fun setUp() {
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `200レスポンスのtag_nameをAppReleaseとして返す`() = runBlocking {
        server.enqueue(
            MockResponse().setBody("""{"tag_name":"v1.2.3","name":"Release 1.2.3"}"""),
        )
        val repository = GitHubAppReleaseRepository(server.url("/releases/latest").toString())

        val release = repository.getLatestRelease()

        assertEquals("v1.2.3", release?.tagName)
    }

    @Test
    fun `404レスポンスのときnullを返す`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        val repository = GitHubAppReleaseRepository(server.url("/releases/latest").toString())

        assertNull(repository.getLatestRelease())
    }

    @Test
    fun `tag_nameが含まれないJSONのときnullを返す`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"message":"Not Found"}"""))
        val repository = GitHubAppReleaseRepository(server.url("/releases/latest").toString())

        assertNull(repository.getLatestRelease())
    }

    @Test
    fun `接続失敗のときnullを返す`() = runBlocking {
        server.shutdown()
        val repository = GitHubAppReleaseRepository("http://localhost:1/releases/latest")

        assertNull(repository.getLatestRelease())
    }
}
