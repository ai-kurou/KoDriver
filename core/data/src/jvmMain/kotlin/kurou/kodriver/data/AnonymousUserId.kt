package kurou.kodriver.data

import java.io.File
import java.util.UUID

/**
 * AnonymousUserId の singleton。
 */
public object AnonymousUserId {
    private const val FILE_NAME = "anonymous_user_id"

    public fun getOrCreate(directory: String): String {
        val file = File(directory, FILE_NAME)
        val existing = if (file.exists()) file.readText().trim() else ""
        if (existing.isNotEmpty()) return existing

        val id = UUID.randomUUID().toString()
        file.parentFile?.mkdirs()
        file.writeText(id)
        return id
    }
}
