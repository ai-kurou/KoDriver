@file:Suppress("FunctionNaming")

package kurou.kodriver.domain

import kotlin.test.Test

class ErrorCaptureTest {
    @Test
    fun `captureDomainErrorを呼んでも例外が発生しない`() {
        captureDomainError(RuntimeException("test"))
    }
}
