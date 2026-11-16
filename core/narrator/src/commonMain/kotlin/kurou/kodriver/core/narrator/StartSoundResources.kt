package kurou.kodriver.core.narrator

import kurou.kodriver.core.narrator.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

public const val FORMULA_RADIO_START_SOUND_PATH: String = "files/formula_radio.wav"
public const val ELECTRONIC_NOISE_START_SOUND_PATH: String = "files/electronic_noise.wav"

@OptIn(ExperimentalResourceApi::class)
public suspend fun readStartSoundBytes(path: String): ByteArray = Res.readBytes(path)
