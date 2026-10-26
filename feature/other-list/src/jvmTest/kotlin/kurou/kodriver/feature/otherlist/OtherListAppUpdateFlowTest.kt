package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListAppUpdateFlowTest {
    private val repository: AppUpdateRepository = mockk()

    @Test
    fun `新しいバージョンがある場合更新ありを通知する`() =
        runTest {
            coEvery { repository.getLatestRelease() } returns AppUpdate(tagName = "v9.9.9")
            val flow = createFlow()

            assertEquals(OtherListAppUpdateUiState(), flow.uiState.value)
            flow.checkUpdate()

            assertEquals(OtherListAppUpdateUiState(hasAppUpdate = true), flow.uiState.value)
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新またはリリース情報なしの場合更新なしを通知して再確認しない`() =
        runTest {
            coEvery { repository.getLatestRelease() } returnsMany listOf(AppUpdate(tagName = "v1.0.0"), null)
            val flow = createFlow()

            flow.checkUpdate()
            flow.checkUpdate()
            assertEquals(OtherListAppUpdateUiState(), flow.uiState.value)

            val noReleaseFlow = createFlow()
            noReleaseFlow.checkUpdate()
            noReleaseFlow.checkUpdate()
            assertEquals(OtherListAppUpdateUiState(), noReleaseFlow.uiState.value)
            coVerify(exactly = 2) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `バージョンが空の場合更新確認を行わない`() =
        runTest {
            val flow = createFlow(currentVersion = " ")

            flow.checkUpdate()

            assertEquals(OtherListAppUpdateUiState(), flow.uiState.value)
            coVerify(exactly = 0) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `確認中および確認完了後の再呼び出しでは一度だけ確認する`() =
        runTest {
            val release = CompletableDeferred<AppUpdate?>()
            coEvery { repository.getLatestRelease() } coAnswers { release.await() }
            val flow = createFlow()

            val check = backgroundScope.launch { flow.checkUpdate() }
            runCurrent()
            flow.checkUpdate()
            release.complete(AppUpdate(tagName = "v9.9.9"))
            check.join()
            flow.checkUpdate()

            assertEquals(OtherListAppUpdateUiState(hasAppUpdate = true), flow.uiState.value)
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `確認が失敗しても再確認せず例外を呼び出し元に返す`() =
        runTest {
            coEvery { repository.getLatestRelease() } throws IllegalStateException("取得失敗")
            val flow = createFlow()

            assertFailsWith<IllegalStateException> { flow.checkUpdate() }
            flow.checkUpdate()

            assertEquals(OtherListAppUpdateUiState(), flow.uiState.value)
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    private fun createFlow(currentVersion: String = "1.0.0") =
        OtherListAppUpdateFlow(CheckAppUpdateAvailableUseCase(repository), currentVersion)
}
