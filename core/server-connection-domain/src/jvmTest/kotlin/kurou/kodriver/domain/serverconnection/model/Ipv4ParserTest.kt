package kurou.kodriver.domain.serverconnection.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Ipv4ParserTest {
    @Test
    fun `正しいIPv4アドレスは4つのオクテットに変換する`() {
        assertEquals(listOf(0, 0, 0, 0), parseIpv4Octets("0.0.0.0"))
        assertEquals(listOf(192, 168, 10, 1), parseIpv4Octets("192.168.10.1"))
        assertEquals(listOf(255, 255, 255, 255), parseIpv4Octets("255.255.255.255"))
    }

    @Test
    fun `区切りが4つでなければnullを返す`() {
        listOf("", "192.168.1", "192.168.1.1.1", "192.168.1.1.").forEach { assertNull(parseIpv4Octets(it), it) }
    }

    @Test
    fun `範囲外や数字以外を含むオクテットはnullを返す`() {
        listOf("192.168.1.256", "192.168.1.1000", "192.168.a.1", "192..1.1", " 192.168.1.1", "192.168.1.1\n")
            .forEach { assertNull(parseIpv4Octets(it), it) }
    }

    @Test
    fun `符号が付いたオクテットはnullを返す`() {
        listOf("+1.+2.+3.+4", "-0.0.0.0", "192.168.1.-1").forEach { assertNull(parseIpv4Octets(it), it) }
    }

    @Test
    fun `ASCII以外の数字はnullを返す`() {
        listOf("１９２.168.1.2", "192.168.1.٢").forEach { assertNull(parseIpv4Octets(it), it) }
    }

    @Test
    fun `先頭ゼロがあるオクテットはnullを返す`() {
        listOf("010.0.0.1", "192.168.1.01", "00.0.0.0").forEach { assertNull(parseIpv4Octets(it), it) }
    }
}
