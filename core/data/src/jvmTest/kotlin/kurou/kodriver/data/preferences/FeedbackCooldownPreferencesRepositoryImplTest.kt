package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedbackCooldownPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_feedback_cooldown_repo_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createRepository() =
        FeedbackCooldownPreferencesRepositoryImpl(
            dataStore = createFeedbackCooldownDataStore(tempDir.absolutePath),
        )

    @Test
    fun `送信日時を保存して取得できる`() =
        runTest {
            val repository = createRepository()
            repository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L)

            assertEquals(1_700_000_000_000L, repository.lastFeedbackSentAtEpochMillis().first())
        }

    @Test
    fun `未保存の場合はnullを返す`() =
        runTest {
            val repository = createRepository()

            assertNull(repository.lastFeedbackSentAtEpochMillis().first())
        }

    @Test
    fun `送信日時を上書き保存できる`() =
        runTest {
            val repository = createRepository()
            repository.saveLastFeedbackSentAtEpochMillis(1_600_000_000_000L)
            repository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L)

            assertEquals(1_700_000_000_000L, repository.lastFeedbackSentAtEpochMillis().first())
        }

    @Test
    fun `feedback_cooldown_pbに書き込まれる`() =
        runTest {
            val repository = createRepository()
            repository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L)

            val file = tempDir.resolve("feedback_cooldown.pb")
            assert(file.exists()) { "feedback_cooldown.pb が作成されていない" }
        }
}
