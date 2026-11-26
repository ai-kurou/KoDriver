package kurou.kodriver.domain.model

/** マイクから取得するPCM音声のサンプリング周波数（Hz）。 */
public const val MICROPHONE_SAMPLE_RATE_HZ: Int = 16_000

/** マイクから取得するPCM音声のチャンネル数（モノラル）。 */
public const val MICROPHONE_CHANNEL_COUNT: Int = 1

/** マイクから取得するPCM音声の1サンプルのビット数（符号付き・リトルエンディアン）。 */
public const val MICROPHONE_SAMPLE_SIZE_BITS: Int = 16
