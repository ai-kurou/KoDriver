package kurou.kodriver.core.gt7ps5data.datasource

internal object Salsa20 {

    fun decrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        require(iv.size == 8) { "IV must be 8 bytes" }

        val state = IntArray(16)
        state[0] = SIGMA_0
        state[1] = key.leInt(0)
        state[2] = key.leInt(4)
        state[3] = key.leInt(8)
        state[4] = key.leInt(12)
        state[5] = SIGMA_1
        state[6] = iv.leInt(0)
        state[7] = iv.leInt(4)
        state[8] = 0 // counter low
        state[9] = 0 // counter high
        state[10] = SIGMA_2
        state[11] = key.leInt(16)
        state[12] = key.leInt(20)
        state[13] = key.leInt(24)
        state[14] = key.leInt(28)
        state[15] = SIGMA_3

        val output = ByteArray(data.size)
        val block = IntArray(16)
        var pos = 0

        while (pos < data.size) {
            block.copyFrom(state)
            repeat(10) { doubleRound(block) }
            for (i in 0..15) block[i] = block[i] + state[i]

            val keystream = IntArray(16) { block[it] }
            val remaining = minOf(64, data.size - pos)
            for (i in 0 until remaining) {
                val word = keystream[i / 4]
                val shift = (i % 4) * 8
                output[pos + i] = (data[pos + i].toInt() xor (word ushr shift)).toByte()
            }

            pos += 64
            state[8]++
            if (state[8] == 0) state[9]++
        }

        return output
    }

    private fun doubleRound(x: IntArray) {
        // column round
        x[4] = x[4] xor rotl(x[0] + x[12], 7)
        x[8] = x[8] xor rotl(x[4] + x[0], 9)
        x[12] = x[12] xor rotl(x[8] + x[4], 13)
        x[0] = x[0] xor rotl(x[12] + x[8], 18)
        x[9] = x[9] xor rotl(x[5] + x[1], 7)
        x[13] = x[13] xor rotl(x[9] + x[5], 9)
        x[1] = x[1] xor rotl(x[13] + x[9], 13)
        x[5] = x[5] xor rotl(x[1] + x[13], 18)
        x[14] = x[14] xor rotl(x[10] + x[6], 7)
        x[2] = x[2] xor rotl(x[14] + x[10], 9)
        x[6] = x[6] xor rotl(x[2] + x[14], 13)
        x[10] = x[10] xor rotl(x[6] + x[2], 18)
        x[3] = x[3] xor rotl(x[15] + x[11], 7)
        x[7] = x[7] xor rotl(x[3] + x[15], 9)
        x[11] = x[11] xor rotl(x[7] + x[3], 13)
        x[15] = x[15] xor rotl(x[11] + x[7], 18)
        // row round
        x[1] = x[1] xor rotl(x[0] + x[3], 7)
        x[2] = x[2] xor rotl(x[1] + x[0], 9)
        x[3] = x[3] xor rotl(x[2] + x[1], 13)
        x[0] = x[0] xor rotl(x[3] + x[2], 18)
        x[6] = x[6] xor rotl(x[5] + x[4], 7)
        x[7] = x[7] xor rotl(x[6] + x[5], 9)
        x[4] = x[4] xor rotl(x[7] + x[6], 13)
        x[5] = x[5] xor rotl(x[4] + x[7], 18)
        x[11] = x[11] xor rotl(x[10] + x[9], 7)
        x[8] = x[8] xor rotl(x[11] + x[10], 9)
        x[9] = x[9] xor rotl(x[8] + x[11], 13)
        x[10] = x[10] xor rotl(x[9] + x[8], 18)
        x[12] = x[12] xor rotl(x[15] + x[14], 7)
        x[13] = x[13] xor rotl(x[12] + x[15], 9)
        x[14] = x[14] xor rotl(x[13] + x[12], 13)
        x[15] = x[15] xor rotl(x[14] + x[13], 18)
    }

    private fun rotl(v: Int, n: Int): Int = (v shl n) or (v ushr (32 - n))

    private fun ByteArray.leInt(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 3].toInt() and 0xFF) shl 24)

    private fun IntArray.copyFrom(src: IntArray) {
        src.copyInto(this)
    }

    // "expand 32-byte k" constants split into four little-endian ints
    private val SIGMA_0 = "expa".toByteArray().leInt(0)
    private val SIGMA_1 = "nd 3".toByteArray().leInt(0)
    private val SIGMA_2 = "2-by".toByteArray().leInt(0)
    private val SIGMA_3 = "te k".toByteArray().leInt(0)

    private fun String.leInt(offset: Int): Int = toByteArray(Charsets.US_ASCII).leInt(offset)
}
