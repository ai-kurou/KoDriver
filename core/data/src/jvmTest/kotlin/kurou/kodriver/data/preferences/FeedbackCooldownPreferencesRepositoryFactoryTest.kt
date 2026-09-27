package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.FeedbackCooldownPreferencesRepository
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class FeedbackCooldownPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_feedback_cooldown_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `FeedbackCooldownPreferencesRepositoryを返す`() {
        val repository = createFeedbackCooldownPreferencesRepository(tempDir.absolutePath)

        assertIs<FeedbackCooldownPreferencesRepository>(repository)
    }

    @Test
    fun `初期状態はnullを返す`() =
        runTest {
            val repository = createFeedbackCooldownPreferencesRepository(tempDir.absolutePath)

            assertNull(repository.lastFeedbackSentAtEpochMillis().first())
        }

    @Test
    fun `保存した送信日時を取得できる`() =
        runTest {
            val repository = createFeedbackCooldownPreferencesRepository(tempDir.absolutePath)

            repository.saveLastFeedbackSentAtEpochMillis(1_700_000_000_000L)

            assertEquals(1_700_000_000_000L, repository.lastFeedbackSentAtEpochMillis().first())
        }
}
