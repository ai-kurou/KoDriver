package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ManualReadoutSettingsResetter
import kurou.kodriver.domain.usecase.ResetManualReadoutSettingsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtherListManualReadoutSettingsResetFlowTest : OtherListResetFlowTestContract() {
    private val repository: ManualReadoutSettingsResetter = mockk()
    private val secondResetter: ManualReadoutSettingsResetter = mockk()

    @Test
    fun `登録された全resetterを実行してから成功を通知する`() =
        runTest {
            coEvery { repository.resetToDefault() } returns Unit
            coEvery { secondResetter.resetToDefault() } returns Unit
            val resetFlow =
                OtherListManualReadoutSettingsResetFlow(
                    ResetManualReadoutSettingsUseCase(listOf(repository, secondResetter)),
                )

            resetFlow.onResetClick()
            resetFlow.onResetConfirm()

            assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
            coVerify(exactly = 1) { repository.resetToDefault() }
            coVerify(exactly = 1) { secondResetter.resetToDefault() }
            confirmVerified(repository, secondResetter)
        }

    @Test
    fun `未確認時と取消後の確定では初期化しない`() =
        runTest {
            val resetFlow =
                OtherListManualReadoutSettingsResetFlow(
                    ResetManualReadoutSettingsUseCase(listOf(repository)),
                )
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            resetFlow.onResetConfirm()
            resetFlow.onResetClick()
            assertTrue(resetFlow.uiState.value.showResetConfirmDialog)
            resetFlow.onResetDismiss()
            resetFlow.onResetConfirm()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 0) { repository.resetToDefault() }
            confirmVerified(repository)
        }

    @Test
    fun `初期化中の再クリックと二重確定を抑止し成功結果を消費する`() =
        runTest {
            val completion = CompletableDeferred<Unit>()
            coEvery { repository.resetToDefault() } coAnswers { completion.await() }
            val resetFlow =
                OtherListManualReadoutSettingsResetFlow(
                    ResetManualReadoutSettingsUseCase(listOf(repository)),
                )
            resetFlow.onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
            assertTrue(resetFlow.uiState.value.isResetting)
            assertFalse(resetFlow.uiState.value.showResetConfirmDialog)
            assertNull(resetFlow.uiState.value.resetSucceeded)
            resetFlow.onResetClick()
            resetFlow.onResetConfirm()
            assertFalse(resetFlow.uiState.value.showResetConfirmDialog)
            completion.complete(Unit)
            job.join()
            assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
            resetFlow.consumeResetResult()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 1) { repository.resetToDefault() }
            confirmVerified(repository)
        }

    private val resetFlow =
        OtherListManualReadoutSettingsResetFlow(
            ResetManualReadoutSettingsUseCase(listOf(repository)),
        )

    override val uiState: StateFlow<OtherListResetUiState> = resetFlow.uiState

    override fun onResetClick() = resetFlow.onResetClick()

    override suspend fun onResetConfirm() = resetFlow.onResetConfirm()

    override fun stubFailure(exception: Exception) {
        coEvery { repository.resetToDefault() } throws exception
    }

    override fun stubAwait(completion: CompletableDeferred<Unit>) {
        coEvery { repository.resetToDefault() } coAnswers { completion.await() }
    }

    override fun verifyCalls(
        firstCalls: Int,
        remainingCalls: Int,
    ) {
        coVerify(exactly = firstCalls) { repository.resetToDefault() }
        confirmVerified(repository)
    }
}
