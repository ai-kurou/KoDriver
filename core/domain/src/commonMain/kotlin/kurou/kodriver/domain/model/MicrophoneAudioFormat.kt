package kurou.kodriver.domain.model

/** マイクから取得するPCM音声のサンプリング周波数（Hz）。 */
const val MICROPHONE_SAMPLE_RATE_HZ = 16_000

/** マイクから取得するPCM音声のチャンネル数（モノラル）。 */
const val MICROPHONE_CHANNEL_COUNT = 1

/** マイクから取得するPCM音声の1サンプルのビット数（符号付き・リトルエンディアン）。 */
const val MICROPHONE_SAMPLE_SIZE_BITS = 16
