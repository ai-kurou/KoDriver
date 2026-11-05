package kurou.kodriver.core.speechrecognitiondata.speech

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class VoskModelLocationTest {
    private val userHome = Files.createTempDirectory("vosk-user-home").toFile()
    private val resources = Files.createTempDirectory("vosk-resources").toFile()
    private val userModel = File(userHome, ".kodriver/vosk-model-ja")
    private val bundledModel = File(resources, "vosk-model-ja")

    private fun File.placeModel() {
        File(this, "am").mkdirs()
    }

    @Test
    fun `手動配置のモデルがあれば同梱より優先する`() {
        userModel.placeModel()
        bundledModel.placeModel()

        assertEquals(userModel, resolveVoskModelDirectory(userHome.path, resources.path))
    }

    @Test
    fun `手動配置が無ければ同梱のモデルを使う`() {
        bundledModel.placeModel()

        assertEquals(bundledModel, resolveVoskModelDirectory(userHome.path, resources.path))
    }

    @Test
    fun `手動配置のディレクトリが空や展開途中なら同梱のモデルを使う`() {
        userModel.mkdirs()
        bundledModel.placeModel()

        assertEquals(bundledModel, resolveVoskModelDirectory(userHome.path, resources.path))
    }

    @Test
    fun `どちらも無ければ手動配置の場所を返す`() {
        assertEquals(userModel, resolveVoskModelDirectory(userHome.path, resources.path))
    }

    @Test
    fun `同梱リソースのディレクトリが無ければ手動配置の場所だけを見る`() {
        assertEquals(userModel, resolveVoskModelDirectory(userHome.path, null))
    }
}
