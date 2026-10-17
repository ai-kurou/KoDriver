package kurou.kodriver.core.windowsstartupdata.windows

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RegistryStartupRegistrySourceTest {
    private val registry = FakeStartupRegistryApi()
    private val entry = "Software\\Microsoft\\Windows\\CurrentVersion\\Run" to "KoDriver"
    private val command = "C:\\Program Files\\KoDriver\\KoDriver.exe"
    private val source = RegistryStartupRegistrySource(registry) { command }

    @Test
    fun `登録済みの場合trueを返す`() {
        registry.values[entry] = command

        assertTrue(source.isRegistered())
    }

    @Test
    fun `未登録の場合falseを返す`() {
        assertFalse(source.isRegistered())
    }

    @Test
    fun `取得したコマンドを所定のキーと値名へ登録する`() {
        source.register()

        assertEquals(mapOf(entry to command), registry.values)
    }

    @Test
    fun `標準のコマンド取得結果に応じて登録または例外を返す`() {
        val source = RegistryStartupRegistrySource(registry)
        val expectedCommand =
            ProcessHandle
                .current()
                .info()
                .command()
                .orElse(null)

        if (expectedCommand == null) {
            val exception = assertFailsWith<IllegalStateException> { source.register() }

            assertEquals("実行中のアプリケーションの起動コマンドを取得できません", exception.message)
            assertTrue(registry.values.isEmpty())
        } else {
            source.register()

            assertEquals(mapOf(entry to expectedCommand), registry.values)
        }
    }

    @Test
    fun `コマンドを取得できない場合は書き込まず例外を投げる`() {
        val source = RegistryStartupRegistrySource(registry) { null }

        val exception = assertFailsWith<IllegalStateException> { source.register() }

        assertEquals("実行中のアプリケーションの起動コマンドを取得できません", exception.message)
        assertTrue(registry.values.isEmpty())
    }

    @Test
    fun `登録済みの場合だけ対象の値を削除する`() {
        registry.values[entry] = command
        val otherEntry = entry.first to "OtherApp"
        registry.values[otherEntry] = "other.exe"

        source.unregister()

        assertEquals(listOf(entry), registry.deletedValues)
        assertEquals(mapOf(otherEntry to "other.exe"), registry.values)
    }

    @Test
    fun `未登録の場合は削除しない`() {
        source.unregister()

        assertTrue(registry.deletedValues.isEmpty())
    }

    @Test
    fun `存在確認の失敗を呼び出し元へ伝える`() {
        val failure = IllegalStateException("registry unavailable")
        registry.failure = failure

        assertSame(failure, assertFailsWith<IllegalStateException> { source.isRegistered() })
    }

    @Test
    fun `書き込みの失敗を呼び出し元へ伝える`() {
        val failure = IllegalStateException("registry unavailable")
        registry.failure = failure

        assertSame(failure, assertFailsWith<IllegalStateException> { source.register() })
    }

    @Test
    fun `解除時の存在確認失敗を伝えて削除しない`() {
        val failure = IllegalStateException("registry unavailable")
        registry.failure = failure

        assertSame(failure, assertFailsWith<IllegalStateException> { source.unregister() })
        assertTrue(registry.deletedValues.isEmpty())
    }

    @Test
    fun `コマンド取得の失敗を伝えて書き込まない`() {
        val failure = IllegalStateException("process unavailable")
        val source = RegistryStartupRegistrySource(registry) { throw failure }

        assertSame(failure, assertFailsWith<IllegalStateException> { source.register() })
        assertTrue(registry.values.isEmpty())
    }

    @Test
    fun `削除の失敗を呼び出し元へ伝える`() {
        registry.values[entry] = command
        val failure = IllegalStateException("delete failed")
        registry.deletionFailure = failure

        assertSame(failure, assertFailsWith<IllegalStateException> { source.unregister() })
        assertEquals(mapOf(entry to command), registry.values)
    }
}
