@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.speech

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VoskModelInstallerTest {
    private val root = createTempDirectory("vosk-model-installer").toFile()
    private val target = File(root, "vosk-model-ja")
    private var copyCount = 0

    @AfterTest
    fun tearDown() {
        root.deleteRecursively()
    }

    private fun installer(
        version: String,
        content: String = "model",
    ) = VoskModelInstaller(target, version) {
        copyCount++
        it.mkdirs()
        File(it, "am").writeText(content)
    }

    @Test
    fun `未展開なら展開し、マーカーにversionを書く`() {
        val result = installer("v1").install()

        assertEquals(target, result)
        assertEquals(1, copyCount)
        assertEquals("model", File(target, "am").readText())
        assertEquals("v1", File(target, MODEL_READY_MARKER).readText())
    }

    @Test
    fun `同じversionで展開済みなら展開し直さない`() {
        installer("v1").install()

        installer("v1").install()

        assertEquals(1, copyCount)
    }

    @Test
    fun `versionが変わったら古い内容を消して展開し直す`() {
        installer("v1", content = "old").install()
        File(target, "stale").writeText("残らないはずのファイル")

        installer("v2", content = "new").install()

        assertEquals(2, copyCount)
        assertEquals("new", File(target, "am").readText())
        assertFalse(File(target, "stale").exists())
        assertEquals("v2", File(target, MODEL_READY_MARKER).readText())
    }

    @Test
    fun `以前の版の空のマーカーは別versionとして展開し直す`() {
        target.mkdirs()
        File(target, MODEL_READY_MARKER).createNewFile()

        installer("v1").install()

        assertEquals(1, copyCount)
        assertEquals("v1", File(target, MODEL_READY_MARKER).readText())
    }

    @Test
    fun `マーカーが無い展開途中のディレクトリは消して展開し直す`() {
        target.mkdirs()
        File(target, "partial").writeText("途中")

        installer("v1").install()

        assertEquals(1, copyCount)
        assertFalse(File(target, "partial").exists())
        assertTrue(File(target, MODEL_READY_MARKER).isFile)
    }

    @Test
    fun `展開に失敗したらマーカーを書かず、次回に展開し直す`() {
        assertFailsWith<IllegalStateException> {
            VoskModelInstaller(target, "v1") { throw IllegalStateException("copy failed") }.install()
        }
        assertFalse(File(target, MODEL_READY_MARKER).exists())

        installer("v1").install()

        assertEquals(1, copyCount)
    }
}
