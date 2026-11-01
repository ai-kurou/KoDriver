package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ResetAutoReadoutSettingsUseCaseTest {
    private val firstResetter: AutoReadoutSettingsResetter = mockk()
    private val secondResetter: AutoReadoutSettingsResetter = mockk()
    private val lastResetter: AutoReadoutSettingsResetter = mockk()
    private val useCase = ResetAutoReadoutSettingsUseCase(listOf(firstResetter, secondResetter, lastResetter))

    @Test
    fun `渡された順にすべてのresetterを一度ずつ実行する`() =
        runTest {
            val calls = mutableListOf<String>()
            coEvery { firstResetter.resetToDefault() } answers {
                calls += "first"
            }
            coEvery { secondResetter.resetToDefault() } answers {
                calls += "second"
            }
            coEvery { lastResetter.resetToDefault() } answers {
                calls += "last"
            }

            useCase()

            assertEquals(listOf("first", "second", "last"), calls)
            coVerify(exactly = 1) { firstResetter.resetToDefault() }
            coVerify(exactly = 1) { secondResetter.resetToDefault() }
            coVerify(exactly = 1) { lastResetter.resetToDefault() }
            confirmVerified(firstResetter, secondResetter, lastResetter)
        }

    @Test
    fun `resetterが空でも正常に終了する`() =
        runTest {
            ResetAutoReadoutSettingsUseCase(emptyList())()
        }

    @Test
    fun `途中の例外を伝播して後続のresetterを実行しない`() =
        runTest {
            val failure = IllegalStateException("reset failed")
            coEvery { firstResetter.resetToDefault() } returns Unit
            coEvery { secondResetter.resetToDefault() } throws failure

            assertSame(failure, assertFailsWith<IllegalStateException> { useCase() })

            coVerify(exactly = 1) { firstResetter.resetToDefault() }
            coVerify(exactly = 1) { secondResetter.resetToDefault() }
            coVerify(exactly = 0) { lastResetter.resetToDefault() }
            confirmVerified(firstResetter, secondResetter, lastResetter)
        }

    @Test
    fun `キャンセルを伝播して後続のresetterを実行しない`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { firstResetter.resetToDefault() } throws cancellation

            assertSame(cancellation, assertFailsWith<CancellationException> { useCase() })

            coVerify(exactly = 1) { firstResetter.resetToDefault() }
            coVerify(exactly = 0) { secondResetter.resetToDefault() }
            coVerify(exactly = 0) { lastResetter.resetToDefault() }
            confirmVerified(firstResetter, secondResetter, lastResetter)
        }
}
