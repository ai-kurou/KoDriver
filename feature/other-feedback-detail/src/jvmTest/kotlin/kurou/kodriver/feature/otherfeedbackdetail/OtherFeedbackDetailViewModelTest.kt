@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.repository.FeedbackRepository
import kurou.kodriver.domain.usecase.SendFeedbackUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherFeedbackDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: FeedbackRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OtherFeedbackDetailViewModel(SendFeedbackUseCase(repository))

    @Test
    fun `必須項目が空なら送信せずエラーを表示する`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onSend()

            assertTrue(viewModel.uiState.value.showMessageError)
            assertTrue(viewModel.uiState.value.showNameError)
            assertTrue(viewModel.uiState.value.showEmailError)
            coVerify(exactly = 0) { repository.send(any()) }
            confirmVerified(repository)
        }

    @Test
    fun `入力したフィードバックを送信できる`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            val viewModel = createViewModel()

            viewModel.onTypeSelected(FeedbackType.FeatureRequest)
            viewModel.onMessageChanged("改善してほしいです")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()

            assertTrue(viewModel.uiState.value.isSent)
            assertFalse(viewModel.uiState.value.isSending)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.FeatureRequest,
                        message = "改善してほしいです",
                        name = "Kurou",
                        email = "user@example.com",
                        includesDiagnostics = true,
                    ),
                )
            }
            confirmVerified(repository)
        }

    @Test
    fun `送信に失敗したらエラーを表示する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.failure(IllegalStateException("failed"))
            val viewModel = createViewModel()

            viewModel.onMessageChanged("失敗します")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()

            assertFalse(viewModel.uiState.value.isSent)
            assertTrue(viewModel.uiState.value.sendFailed)
            assertEquals("失敗します", viewModel.uiState.value.message)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "失敗します",
                        name = "Kurou",
                        email = "user@example.com",
                        includesDiagnostics = true,
                    ),
                )
            }
            confirmVerified(repository)
        }
}
