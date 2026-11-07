import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Voskの日本語モデルを配布元から取得してSHA-256を検証し、`vosk-model-ja`ディレクトリとして展開する。
 *
 * 取得したzipは`cacheDir`にチェックサム名で保存し、次回以降は検証が通れば再取得しない（配布元の停止やネットワーク断でビルドが失敗しにくくする）。
 * 取得に失敗した場合は[DOWNLOAD_MAX_ATTEMPTS]回まで間隔を空けて再試行する。
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

        /** 取得したzipの保存先。ビルドディレクトリの外（Gradleユーザーホーム配下など）を指定すると、クリーンビルドでも再取得しない。 */
        @get:Internal
        abstract val cacheDir: DirectoryProperty

        @get:OutputDirectory
        abstract val outputDir: DirectoryProperty

        @TaskAction
        fun prepare() {
            val zip = obtainVerifiedZip()
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

        /** 検証済みのキャッシュがあればそれを、なければ配布元から取得して検証・保存したzipを返す。 */
        private fun obtainVerifiedZip(): File {
            val expected = sha256.get()
            val cached = cacheDir.get().asFile.resolve("$expected.zip")
            if (cached.isFile) {
                if (sha256Of(cached) == expected) return cached
                // 壊れたキャッシュは捨てて取得し直す
                cached.delete()
            }
            val downloaded = downloadWithRetry()
            val actual = sha256Of(downloaded)
            check(actual == expected) {
                "Voskモデルのチェックサムが一致しません。期待値=$expected 実際=$actual"
            }
            cached.parentFile.mkdirs()
            // 書き込み途中のファイルをキャッシュとして読まないよう、完成してから置き換える
            downloaded.copyTo(cached, overwrite = true)
            return cached
        }

        private fun downloadWithRetry(): File {
            val zip = temporaryDir.resolve("vosk-model.zip")
            val uri = URI(modelUrl.get())
            // ビルドスクリプトで固定した配布元（HTTPS）以外からは取得しない
            require(uri.scheme == "https" && uri.host == "alphacephei.com") { "想定外のモデル取得元です: $uri" }
            var lastError: Exception? = null
            repeat(DOWNLOAD_MAX_ATTEMPTS) { attempt ->
                try {
                    download(uri, zip)
                    return zip
                } catch (e: java.io.IOException) {
                    lastError = e
                    logger.warn("Voskモデルの取得に失敗しました（${attempt + 1}/${DOWNLOAD_MAX_ATTEMPTS}回目）: ${e.javaClass.simpleName}")
                    if (attempt < DOWNLOAD_MAX_ATTEMPTS - 1) Thread.sleep(DOWNLOAD_RETRY_DELAY_MILLIS * (attempt + 1))
                }
            }
            throw IllegalStateException("Voskモデルを取得できませんでした: $uri", lastError)
        }

        private fun download(
            uri: URI,
            zip: File,
        ) {
            // 配布元の応答が止まってもビルドが終わらなくならないよう、接続・読み取りの待ち時間に上限を設ける
            val connection =
                uri.toURL().openConnection().apply {
                    connectTimeout = TIMEOUT_MILLIS
                    readTimeout = TIMEOUT_MILLIS
                }
            connection.getInputStream().use { input ->
                zip.outputStream().use { output -> input.copyTo(output) }
            }
        }

        private fun sha256Of(file: File): String =
            MessageDigest
                .getInstance("SHA-256")
                .digest(file.readBytes())
                .joinToString("") { "%02x".format(it) }

        private companion object {
            const val TIMEOUT_MILLIS = 60_000
            const val DOWNLOAD_MAX_ATTEMPTS = 3
            const val DOWNLOAD_RETRY_DELAY_MILLIS = 5_000L
        }
    }
