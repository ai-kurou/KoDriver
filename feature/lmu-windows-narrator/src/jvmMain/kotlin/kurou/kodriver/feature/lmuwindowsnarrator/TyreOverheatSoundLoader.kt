package kurou.kodriver.feature.lmuwindowsnarrator

import java.io.File

internal actual suspend fun loadTyreOverheatSound(): ByteArray? {
    val file = File(System.getProperty("user.home"), "Downloads/tyre_overheat.wav")
    return if (file.exists()) file.readBytes() else null
}
