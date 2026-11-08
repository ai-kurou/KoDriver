package kurou.kodriver.core.speechrecognitiondata.speech

import java.io.File

/** モデルの展開が最後まで終わったことを示す、展開先に置くファイル名。中身は展開したときの [VoskModelInstaller] の version。 */
internal const val MODEL_READY_MARKER = ".ready"

/**
 * 同梱したモデルを端末の内部ストレージ（[target]）へ展開する。
 *
 * 展開済みかは、[MODEL_READY_MARKER] があり、その中身が [version] と一致するかで判断する。
 * 展開途中で止まったとき（マーカーが無い）や、アプリの更新で同梱モデルが変わったとき（version が違う）は、
 * 古い内容を残さないよう消してから展開し直す。マーカーは展開が最後まで終わってから書く。
 */
internal class VoskModelInstaller(
    private val target: File,
    private val version: String,
    private val copyModel: (File) -> Unit,
) {
    fun install(): File {
        val marker = File(target, MODEL_READY_MARKER)
        if (!marker.isFile || marker.readText() != version) {
            target.deleteRecursively()
            copyModel(target)
            marker.writeText(version)
        }
        return target
    }
}
