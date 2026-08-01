package kurou.kodriver.data

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnonymousUserIdTest {
    @Test
    fun `初回はIDを新規生成してファイルに保存する`() {
        val directory = Files.createTempDirectory("kodriver_anonymous_user_id_test").toFile()

        val id = AnonymousUserId.getOrCreate(directory.absolutePath)

        assertTrue(id.isNotBlank())
        assertEquals(id, directory.resolve("anonymous_user_id").readText().trim())
    }

    @Test
    fun `2回目以降は保存済みのIDを返す`() {
        val directory = Files.createTempDirectory("kodriver_anonymous_user_id_test").toFile()

        val first = AnonymousUserId.getOrCreate(directory.absolutePath)
        val second = AnonymousUserId.getOrCreate(directory.absolutePath)

        assertEquals(first, second)
    }
}
