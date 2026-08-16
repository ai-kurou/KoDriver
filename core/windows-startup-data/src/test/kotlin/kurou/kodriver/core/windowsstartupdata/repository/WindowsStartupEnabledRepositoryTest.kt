package kurou.kodriver.core.windowsstartupdata.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WindowsStartupEnabledRepositoryTest {
    @Test
    fun `isEnabledはレジストリに登録済みの場合trueを返す`() =
        runTest {
            val source = FakeWindowsStartupRegistrySource(registered = true)
            val repository = WindowsStartupEnabledRepository(source)

            assertTrue(repository.isEnabled())
        }

    @Test
    fun `isEnabledはレジストリに未登録の場合falseを返す`() =
        runTest {
            val source = FakeWindowsStartupRegistrySource(registered = false)
            val repository = WindowsStartupEnabledRepository(source)

            assertFalse(repository.isEnabled())
        }

    @Test
    fun `setEnabledにtrueを渡すとレジストリへ登録する`() =
        runTest {
            val source = FakeWindowsStartupRegistrySource(registered = false)
            val repository = WindowsStartupEnabledRepository(source)

            repository.setEnabled(true)

            assertTrue(source.isRegistered())
        }

    @Test
    fun `setEnabledにfalseを渡すとレジストリから解除する`() =
        runTest {
            val source = FakeWindowsStartupRegistrySource(registered = true)
            val repository = WindowsStartupEnabledRepository(source)

            repository.setEnabled(false)

            assertFalse(source.isRegistered())
        }
}
