package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedbackCooldownDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_feedback_cooldown_ds_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `feedback_cooldown_pbにデータを保存できる`() =
        runTest {
            val dataStore = createFeedbackCooldownDataStore(tempDir.absolutePath)
            val expected = FeedbackCooldownPreferences(lastSentAtEpochMillis = 1_700_000_000_000L)

            dataStore.updateData { expected }

            assertEquals(expected, dataStore.data.first())
        }

    @Test
    fun `ファイルパスはdirectory配下のfeedback_cooldown_pbである`() =
        runTest {
            val dataStore = createFeedbackCooldownDataStore(tempDir.absolutePath)

            dataStore.updateData { FeedbackCooldownPreferences(lastSentAtEpochMillis = 1_700_000_000_000L) }

            val file = tempDir.resolve("feedback_cooldown.pb")
            assert(file.exists()) { "feedback_cooldown.pb が作成されていない" }
        }
}
