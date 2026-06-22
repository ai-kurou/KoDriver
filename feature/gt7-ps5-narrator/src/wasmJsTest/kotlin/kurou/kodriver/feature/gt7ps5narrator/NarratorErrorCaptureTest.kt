@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlin.test.Test

class NarratorErrorCaptureTest {

    @Test
    fun `captureNarratorError を呼んでも例外が発生しない`() {
        captureNarratorError(RuntimeException("test"))
    }
}
