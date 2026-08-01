@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import kotlin.test.Test
import kotlin.test.assertIs

class CreateServerConnectivityCheckerTest {
    @Test
    fun `createServerConnectivityCheckerはTcpServerConnectivityCheckerを返す`() {
        val checker = createServerConnectivityChecker()
        assertIs<TcpServerConnectivityChecker>(checker)
    }
}
