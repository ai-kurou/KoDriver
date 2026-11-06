import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Voskの日本語モデルを配布元から取得してSHA-256を検証し、`vosk-model-ja`ディレクトリとして展開する。
 *
 * 展開先の`outputDir`の`modelPath`に展開する。Desktopは`outputDir`をCompose Desktopの`appResourcesRootDir`にして`common/vosk-model-ja`をインストール先へ実フォルダのまま同梱し、Androidは`outputDir`をassetsにして`vosk-model-ja`を同梱する
 * （VoskのModelはJAR内のリソースを読めず、ディレクトリのパスを要求するため）。
 */
abstract class PrepareVoskModelTask
    @Inject
    constructor(
        private val archives: ArchiveOperations,
        private val fileSystem: FileSystemOperations,
    ) : DefaultTask() {
        @get:Input
        abstract val modelUrl: Property<String>

        @get:Input
        abstract val sha256: Property<String>

        /** [outputDir] からの展開先の相対パス。 */
        @get:Input
        abstract val modelPath: Property<String>

        @get:OutputDirectory
        abstract val outputDir: DirectoryProperty

        @TaskAction
        fun prepare() {
            val zip = temporaryDir.resolve("vosk-model.zip")
            val uri = URI(modelUrl.get())
            // ビルドスクリプトで固定した配布元（HTTPS）以外からは取得しない
            require(uri.scheme == "https" && uri.host == "alphacephei.com") { "想定外のモデル取得元です: $uri" }
            // 配布元の応答が止まってもビルドが終わらなくならないよう、接続・読み取りの待ち時間に上限を設ける
            val timeoutMillis = 60_000
            val connection =
                uri.toURL().openConnection().apply {
                    connectTimeout = timeoutMillis
                    readTimeout = timeoutMillis
                }
            connection.getInputStream().use { input ->
                zip.outputStream().use { output -> input.copyTo(output) }
            }
            val actual =
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(zip.readBytes())
                    .joinToString("") { "%02x".format(it) }
            check(actual == sha256.get()) {
                "Voskモデルのチェックサムが一致しません。期待値=${sha256.get()} 実際=$actual"
            }
            val target = outputDir.get().asFile.resolve(modelPath.get())
            fileSystem.delete { delete(target) }
            fileSystem.copy {
                from(archives.zipTree(zip))
                into(target)
                includeEmptyDirs = false
                // zip直下のディレクトリ（vosk-model-small-ja-0.22）を取り除き、モデルの中身を直接配置する
                eachFile { path = path.substringAfter('/') }
            }
        }
    }
