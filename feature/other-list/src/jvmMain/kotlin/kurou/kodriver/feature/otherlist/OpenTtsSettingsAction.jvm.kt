package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler

/** rememberOpenTtsSettings のJVM実装。Windowsで音声設定を開く。 */
@Composable
actual fun rememberOpenTtsSettings(): () -> Unit {
    val uriHandler = LocalUriHandler.current
    return {
        if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
            uriHandler.openUri("ms-settings:speech")
        }
    }
}
