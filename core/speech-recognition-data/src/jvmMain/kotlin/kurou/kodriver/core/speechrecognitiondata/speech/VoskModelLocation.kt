package kurou.kodriver.core.speechrecognitiondata.speech

import java.io.File

/** Voskの日本語モデルのディレクトリ名。ユーザー配置・アプリ同梱のどちらも、この名前で置く。 */
internal const val VOSK_MODEL_DIRECTORY_NAME = "vosk-model-ja"

/**
 * Voskの日本語モデルのディレクトリを解決する。
 *
 * ユーザーが手動で置いたモデル（`<userHome>/.kodriver/vosk-model-ja`）を優先し、無ければアプリに同梱したモデル
 * （`<resourcesDirectory>/vosk-model-ja`）を使う。どちらも無い場合はユーザー配置の場所を返す（利用不可になる）。
 *
 * @param resourcesDirectory アプリ同梱リソースのディレクトリ（`compose.application.resources.dir`）。開発実行などで無ければnull
 */
internal fun resolveVoskModelDirectory(
    userHome: String,
    resourcesDirectory: String?,
): File {
    val userModel = File(userHome, ".kodriver/$VOSK_MODEL_DIRECTORY_NAME")
    val bundledModel = resourcesDirectory?.let { File(it, VOSK_MODEL_DIRECTORY_NAME) }
    return listOfNotNull(userModel, bundledModel).firstOrNull { it.isDirectory } ?: userModel
}
