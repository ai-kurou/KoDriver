package kurou.kodriver.presentation

import java.awt.Desktop
import java.net.URI

/** Windowsでは音声設定を開き、それ以外のJVM環境では何もしない。 */
actual fun openWindowsSpeechSettings() {
    if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
        Desktop.getDesktop().browse(URI("ms-settings:speech"))
    }
}
