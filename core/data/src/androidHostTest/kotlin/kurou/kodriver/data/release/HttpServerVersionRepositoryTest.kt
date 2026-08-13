@file:Suppress("FunctionNaming")

package kurou.kodriver.data.release

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpServerVersionRepositoryTest {
    private lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    private fun buildRepository() = HttpServerVersionRepository(port = server.port)

    @Test
    fun `バージョンエンドポイントが正常なJSONを返すときResultSuccessを返す`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("""{"version":"0.5.0"}"""),
            )
            val result = buildRepository().fetchVersion("127.0.0.1")
            assertEquals("0.5.0", result.getOrNull())
        }

    @Test
    fun `サーバーが404を返すときResultFailureを返す`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            val result = buildRepository().fetchVersion("127.0.0.1")
            assertTrue(result.isFailure)
        }

    @Test
    fun `到達不能なサーバーのときResultFailureを返す`() =
        runTest {
            server.shutdown()
            val result = buildRepository().fetchVersion("127.0.0.1")
            assertTrue(result.isFailure)
        }

    @Test
    fun `予期しないJSONのときResultFailureを返す`() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"unexpected":"value"}"""),
            )
            val result = buildRepository().fetchVersion("127.0.0.1")
            assertTrue(result.isFailure)
        }
}
