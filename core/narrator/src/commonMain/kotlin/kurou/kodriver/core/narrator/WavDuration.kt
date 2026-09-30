package kurou.kodriver.core.narrator

/**
 * WAV（RIFF）のヘッダを解析して再生時間をミリ秒で返す。
 *
 * 再生時間が判定できない場合（WAV ではない・ヘッダが壊れている・`data` チャンクが無い等）は `null` を返す。
 * 呼び出し側は `null` を「不明」として扱い、再生時間に依存した制御へフォールバックを用意すること。
 *
 * `fmt ` チャンクが先頭に無い WAV や、`fmt ` と `data` の間に `LIST` などのチャンクを挟む WAV にも対応するため、
 * `byteRate` を固定オフセットではなくチャンク走査中に見つけた `fmt ` チャンクから読む。
 * また RIFF はチャンクサイズが奇数のとき 1 バイトのパディングが入るため、走査時にその分を加算する。
 * 不正なチャンクサイズ（負値・オーバーフローするような巨大値）を検出した場合は、走査位置が進まず
 * 無限ループになるのを避けるため走査を打ち切る。RIFF は本文 0 バイトのチャンクも許容し、その場合も
 * 8 バイトのチャンクヘッダ分だけ走査位置が進むため、サイズ 0 は不正として扱わない。
 */
@Suppress("ReturnCount")
internal fun wavDurationMs(bytes: ByteArray): Long? {
    if (!bytes.hasRiffWaveHeader()) return null
    var byteRate = 0
    var offset = RIFF_HEADER_SIZE
    while (offset + CHUNK_HEADER_SIZE <= bytes.size) {
        val chunkSize = bytes.readInt32LE(offset + CHUNK_ID_SIZE)
        if (chunkSize < 0) return null
        val bodyOffset = offset + CHUNK_HEADER_SIZE
        when {
            bytes.matchesChunkId(offset, FMT_CHUNK_ID) -> {
                // 宣言サイズが byteRate フィールドに届かない fmt では、後続チャンクの識別子を byteRate として
                // 読んでしまうため、ファイル境界とは別に宣言サイズも検証する。
                if (chunkSize < FMT_BYTE_RATE_OFFSET + INT32_SIZE) return null
                if (bodyOffset + FMT_BYTE_RATE_OFFSET + INT32_SIZE > bytes.size) return null
                byteRate = bytes.readInt32LE(bodyOffset + FMT_BYTE_RATE_OFFSET)
            }

            bytes.matchesChunkId(offset, DATA_CHUNK_ID) -> {
                if (byteRate <= 0) return null
                // 途中で切れた WAV では data のチャンクサイズが実バイト数を超える。
                // そのまま使うと実際の音声より長い再生時間になるため、実バイト数で抑える。
                val dataSize = chunkSize.coerceAtMost(bytes.size - bodyOffset)
                // 本文が空の data は再生すべき音声が無く、0 を返すと即座に停止して無音になるため不明として扱う。
                if (dataSize <= 0) return null
                return dataSize.toLong() * MILLIS_PER_SECOND / byteRate
            }
        }
        // 奇数サイズのチャンクには 1 バイトのパディングが続く。
        val advance = CHUNK_HEADER_SIZE.toLong() + chunkSize + (chunkSize % 2)
        val nextOffset = offset + advance
        if (nextOffset > bytes.size) return null
        offset = nextOffset.toInt()
    }
    return null
}

private fun ByteArray.hasRiffWaveHeader(): Boolean =
    size >= RIFF_HEADER_SIZE &&
        matchesChunkId(0, RIFF_CHUNK_ID) &&
        matchesChunkId(RIFF_FORMAT_OFFSET, WAVE_FORMAT_ID)

/**
 * [offset] から 4 バイトが [id] と一致するか。`Charsets.US_ASCII` は commonMain で使えないためバイト比較する。
 *
 * 呼び出し側が `offset + CHUNK_ID_SIZE <= size` を保証している前提で、ここでは範囲チェックをしない。
 */
private fun ByteArray.matchesChunkId(
    offset: Int,
    id: ByteArray,
): Boolean = id.indices.all { this[offset + it] == id[it] }

private fun ByteArray.readInt32LE(offset: Int): Int =
    (this[offset].toInt() and BYTE_MASK) or
        ((this[offset + 1].toInt() and BYTE_MASK) shl 8) or
        ((this[offset + 2].toInt() and BYTE_MASK) shl 16) or
        ((this[offset + 3].toInt() and BYTE_MASK) shl 24)

private const val BYTE_MASK = 0xFF
private const val INT32_SIZE = 4
private const val MILLIS_PER_SECOND = 1000L

/** `RIFF` + サイズ + `WAVE` までのヘッダ長。チャンク走査の開始位置でもある。 */
private const val RIFF_HEADER_SIZE = 12
private const val RIFF_FORMAT_OFFSET = 8
private const val CHUNK_ID_SIZE = 4

/** チャンク ID（4 バイト）＋ チャンクサイズ（4 バイト）。 */
private const val CHUNK_HEADER_SIZE = 8

/** `fmt ` チャンク本体の先頭から `byteRate` までのオフセット（audioFormat 2 + numChannels 2 + sampleRate 4）。 */
private const val FMT_BYTE_RATE_OFFSET = 8

private val RIFF_CHUNK_ID = "RIFF".encodeToByteArray()
private val WAVE_FORMAT_ID = "WAVE".encodeToByteArray()
private val FMT_CHUNK_ID = "fmt ".encodeToByteArray()
private val DATA_CHUNK_ID = "data".encodeToByteArray()
