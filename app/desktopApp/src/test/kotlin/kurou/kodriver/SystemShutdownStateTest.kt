package kurou.kodriver

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SystemShutdownStateTest {
    @BeforeTest
    fun setUp() {
        SystemShutdownState.reset()
    }

    @AfterTest
    fun tearDown() {
        SystemShutdownState.reset()
    }

    @Test
    fun `初期状態では isShuttingDown が false を返す`() {
        assertFalse(SystemShutdownState.isShuttingDown)
    }

    @Test
    fun `markShuttingDown 後は isShuttingDown が true を返す`() {
        SystemShutdownState.markShuttingDown()

        assertTrue(SystemShutdownState.isShuttingDown)
    }

    @Test
    fun `初期化完了後かつシャットダウン中でない場合は終了確認する`() {
        assertTrue(shouldConfirmExit(ready = true, shuttingDown = false))
    }

    @Test
    fun `シャットダウン中は初期化完了後でも終了確認しない`() {
        assertFalse(shouldConfirmExit(ready = true, shuttingDown = true))
    }

    @Test
    fun `初期化完了前は終了確認しない`() {
        assertFalse(shouldConfirmExit(ready = false, shuttingDown = false))
    }
}
