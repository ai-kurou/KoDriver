package kurou.kodriver.core.narrator

import kurou.kodriver.core.narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

const val FORMULA_RADIO_START_SOUND_PATH = "files/formula_radio.wav"
const val ELECTRONIC_NOISE_START_SOUND_PATH = "files/electronic_noise.wav"

@OptIn(ExperimentalResourceApi::class)
suspend fun readStartSoundBytes(path: String): ByteArray = Res.readBytes(path)
