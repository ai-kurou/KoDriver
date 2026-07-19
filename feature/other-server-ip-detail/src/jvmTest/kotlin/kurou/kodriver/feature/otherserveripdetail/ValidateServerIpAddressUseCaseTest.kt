package kurou.kodriver.feature.otherserveripdetail

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateServerIpAddressUseCaseTest {
    private val useCase = ValidateServerIpAddressUseCase()

    @Test
    fun `各区切りが0から255のIPv4アドレスは有効`() {
        assertTrue(useCase("0.0.0.0"))
        assertTrue(useCase("192.168.1.1"))
        assertTrue(useCase("255.255.255.255"))
    }

    @Test
    fun `区切りが4つでなければ無効`() {
        assertFalse(useCase("192.168.1"))
        assertFalse(useCase("192.168.1.1.1"))
    }

    @Test
    fun `数値でない区切りがあれば無効`() {
        assertFalse(useCase("192.168.a.1"))
    }

    @Test
    fun `範囲外の区切りがあれば無効`() {
        assertFalse(useCase("192.168.1.-1"))
        assertFalse(useCase("192.168.1.256"))
    }
}
