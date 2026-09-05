package kurou.kodriver.core.gt7ps5data.datasource

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class Salsa20Test {
    @Test
    fun `既知のテストベクタで正しく復号できる`() {
        // Salsa20 spec: key=0x00..0x1F, iv=0x00..0x07, all-zero plaintext
        val key = ByteArray(32) { it.toByte() }
        val iv = ByteArray(8) { it.toByte() }
        val plaintext = ByteArray(64)

        val ciphertext = Salsa20.decrypt(key, iv, plaintext)

        val reEncrypted = Salsa20.decrypt(key, iv, ciphertext)
        assertContentEquals(plaintext, reEncrypted)
    }

    @Test
    fun `暗号化と復号は対称である`() {
        val key = "Btta7y3Gp4kH3p3kLmfqAUVsF0YVsPFr".toByteArray(Charsets.US_ASCII)
        val iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
        val original = "Hello GT7 telemetry data".toByteArray(Charsets.US_ASCII)

        val encrypted = Salsa20.decrypt(key, iv, original)
        val decrypted = Salsa20.decrypt(key, iv, encrypted)

        assertContentEquals(original, decrypted)
    }

    @Test
    fun `キーが32バイト未満の場合は例外をスローする`() {
        assertFailsWith<IllegalArgumentException> {
            Salsa20.decrypt(ByteArray(16), ByteArray(8), ByteArray(64))
        }
    }

    @Test
    fun `IVが8バイト未満の場合は例外をスローする`() {
        assertFailsWith<IllegalArgumentException> {
            Salsa20.decrypt(ByteArray(32), ByteArray(4), ByteArray(64))
        }
    }

    @Test
    fun `64バイト境界をまたぐデータを正しく処理できる`() {
        val key = ByteArray(32) { 0x42 }
        val iv = ByteArray(8) { 0x00 }
        val data = ByteArray(200) { it.toByte() }

        val encrypted = Salsa20.decrypt(key, iv, data)
        val decrypted = Salsa20.decrypt(key, iv, encrypted)

        assertContentEquals(data, decrypted)
    }
}
