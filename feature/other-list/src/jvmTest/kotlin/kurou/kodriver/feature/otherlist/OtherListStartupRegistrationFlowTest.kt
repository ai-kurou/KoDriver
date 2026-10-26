package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtherListStartupRegistrationFlowTest {
    private val repository: StartupEnabledRepository = mockk()

    @Test
    fun `初期状態は無効`() {
        val startupRegistrationFlow = createFlow()

        assertEquals(OtherListStartupRegistrationUiState(), startupRegistrationFlow.uiState.value)
        confirmVerified(repository)
    }

    @Test
    fun `状態取得に成功すると取得値を反映する`() =
        runTest {
            coEvery { repository.isEnabled() } returns true
            val startupRegistrationFlow = createFlow()

            startupRegistrationFlow.checkStartupEnabled()

            assertTrue(startupRegistrationFlow.uiState.value.startupEnabled)
            coVerify(exactly = 1) { repository.isEnabled() }
            confirmVerified(repository)
        }

    @Test
    fun `状態取得に失敗すると状態を更新しない`() =
        runTest {
            coEvery { repository.isEnabled() } throws IllegalStateException("取得失敗")
            val startupRegistrationFlow = createFlow()

            assertFailsWith<IllegalStateException> { startupRegistrationFlow.checkStartupEnabled() }

            assertFalse(startupRegistrationFlow.uiState.value.startupEnabled)
            coVerify(exactly = 1) { repository.isEnabled() }
            confirmVerified(repository)
        }

    @Test
    fun `変更に成功すると指定値を反映する`() =
        runTest {
            coEvery { repository.setEnabled(true) } returns Unit
            val startupRegistrationFlow = createFlow()

            startupRegistrationFlow.onStartupEnabledChange(true)

            assertTrue(startupRegistrationFlow.uiState.value.startupEnabled)
            coVerify(exactly = 1) { repository.setEnabled(true) }
            confirmVerified(repository)
        }

    @Test
    fun `変更に失敗すると状態を更新しない`() =
        runTest {
            coEvery { repository.setEnabled(true) } throws IllegalStateException("変更失敗")
            val startupRegistrationFlow = createFlow()

            assertFailsWith<IllegalStateException> { startupRegistrationFlow.onStartupEnabledChange(true) }

            assertFalse(startupRegistrationFlow.uiState.value.startupEnabled)
            coVerify(exactly = 1) { repository.setEnabled(true) }
            confirmVerified(repository)
        }

    private fun createFlow() = OtherListStartupRegistrationFlow(StartupRegistrationUseCases(repository))
}
