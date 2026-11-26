package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MicrophoneAudioEventTest {
    @Test
    fun `Chunkは内容が同じPCMなら等しい`() {
        val chunk = MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2, 3))

        assertEquals(chunk, MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2, 3)))
        assertEquals(chunk.hashCode(), MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2, 3)).hashCode())
        assertNotEquals(chunk, MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2)))
        assertNotEquals<Any>(chunk, "chunk")
    }

    @Test
    fun `Chunkの文字列表現はPCMの内容を含めずサイズを示す`() {
        assertEquals("Chunk(size=3)", MicrophoneAudioEvent.Chunk(byteArrayOf(1, 2, 3)).toString())
    }
}
