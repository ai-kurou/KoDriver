package kurou.kodriver.core.designsystem

import kurou.kodriver.core.designsystem.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
suspend fun readStartSoundBytes(path: String): ByteArray = Res.readBytes(path)
