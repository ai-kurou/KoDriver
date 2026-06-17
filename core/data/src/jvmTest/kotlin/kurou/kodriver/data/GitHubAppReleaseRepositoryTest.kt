package kurou.kodriver.data

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubAppReleaseRepositoryTest {

    @Test
    fun `tag_nameを含むJSONのときAppUpdateとして返す`() = runBlocking {
        val repository = GitHubAppReleaseRepository(
            fetch = { """{"tag_name":"v1.2.3","name":"Release 1.2.3"}""" },
        )

        val update = repository.getLatestRelease()

        assertEquals("v1.2.3", update?.tagName)
    }

    @Test
    fun `fetchがnullを返すときnullを返す`() = runBlocking {
        val repository = GitHubAppReleaseRepository(fetch = { null })

        assertNull(repository.getLatestRelease())
    }

    @Test
    fun `tag_nameが含まれないJSONのときnullを返す`() = runBlocking {
        val repository = GitHubAppReleaseRepository(
            fetch = { """{"message":"Not Found"}""" },
        )

        assertNull(repository.getLatestRelease())
    }

    @Test
    fun `fetchが例外をスローするときnullを返す`() = runBlocking {
        val repository = GitHubAppReleaseRepository(fetch = { error("network error") })

        assertNull(repository.getLatestRelease())
    }
}
