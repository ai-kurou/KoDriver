@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.FeedbackSenderRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kurou.kodriver.domain.usecase.SendFeedbackUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherFeedbackDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: FeedbackSenderRepository

    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherFeedbackDetailViewModel(
            SendFeedbackUseCase(repository),
            ObserveTelemetryLogDetailUseCase(telemetryLogRepository),
        )

    private fun telemetryLog(
        id: Long,
        telemetryJson: String = "",
    ) = TelemetryLog(
        id = id,
        createdAt = 0L,
        simulator = Simulator.LmuWindows,
        readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
        telemetryJson = telemetryJson,
    )

    @Test
    fun `必須項目が空なら送信せずエラーを表示する`() =
        runTest {
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.onSend()

            assertTrue(viewModel.uiState.value.showMessageError)
            assertTrue(viewModel.uiState.value.showNameError)
            assertTrue(viewModel.uiState.value.showEmailError)
            coVerify(exactly = 0) { repository.send(any()) }
            confirmVerified(repository)
            collectionJob.cancel()
        }

    @Test
    fun `メールアドレスの形式が不正なら送信せずエラーを表示する`() =
        runTest {
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.onMessageChanged("本文")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("invalid-email")
            viewModel.onSend()

            assertTrue(viewModel.uiState.value.showEmailError)
            assertFalse(viewModel.uiState.value.showMessageError)
            assertFalse(viewModel.uiState.value.showNameError)
            coVerify(exactly = 0) { repository.send(any()) }
            confirmVerified(repository)
            collectionJob.cancel()
        }

    @Test
    fun `入力したフィードバックを送信できる`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.success(Unit)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

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
            collectionJob.cancel()
        }

    @Test
    fun `送信に失敗したらエラーを表示する`() =
        runTest {
            coEvery { repository.send(any()) } returns Result.failure(IllegalStateException("failed"))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

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
            collectionJob.cancel()
        }

    @Test
    fun `送信中に想定外の例外が発生したらエラーを表示する`() =
        runTest {
            coEvery { repository.send(any()) } throws IllegalStateException("unexpected")
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.onMessageChanged("失敗します")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()

            assertFalse(viewModel.uiState.value.isSent)
            assertFalse(viewModel.uiState.value.isSending)
            assertTrue(viewModel.uiState.value.sendFailed)
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
            collectionJob.cancel()
        }

    @Test
    fun `送信中に再送信してもRepositoryは1回だけ呼ばれる`() =
        runTest {
            val deferredResult = CompletableDeferred<Result<Unit>>()
            coEvery { repository.send(any()) } coAnswers { deferredResult.await() }
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.onMessageChanged("送信します")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()
            viewModel.onSend()

            assertTrue(viewModel.uiState.value.isSending)
            assertFalse(viewModel.uiState.value.canSend)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "送信します",
                        name = "Kurou",
                        email = "user@example.com",
                        includesDiagnostics = true,
                    ),
                )
            }
            deferredResult.complete(Result.success(Unit))
            confirmVerified(repository)
            collectionJob.cancel()
        }

    @Test
    fun `最大文字数を超えた入力は切り詰められる`() =
        runTest {
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.onMessageChanged("あ".repeat(FEEDBACK_MESSAGE_MAX_LENGTH + 1))
            viewModel.onNameChanged("あ".repeat(FEEDBACK_NAME_MAX_LENGTH + 1))
            viewModel.onEmailChanged("a".repeat(FEEDBACK_EMAIL_MAX_LENGTH + 1))

            assertEquals(FEEDBACK_MESSAGE_MAX_LENGTH, viewModel.uiState.value.message.length)
            assertEquals(FEEDBACK_NAME_MAX_LENGTH, viewModel.uiState.value.name.length)
            assertEquals(FEEDBACK_EMAIL_MAX_LENGTH, viewModel.uiState.value.email.length)
            collectionJob.cancel()
        }

    @Test
    fun `送信中なら送信できない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                message = "送信します",
                name = "Kurou",
                email = "user@example.com",
                isSending = true,
            )

        assertFalse(uiState.canSend)
    }

    @Test
    fun `setTelemetryLogIdで指定したログが添付される`() =
        runTest {
            val log = telemetryLog(id = 1L)
            every { telemetryLogRepository.observeTelemetryLogDetail(1L) } returns
                flowOf(TelemetryLogDetail(current = log, previous = null))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

            viewModel.setTelemetryLogId(1L)

            assertEquals(log, viewModel.uiState.first { it.attachedTelemetryLog != null }.attachedTelemetryLog)
            verify(exactly = 1) { telemetryLogRepository.observeTelemetryLogDetail(1L) }
            confirmVerified(telemetryLogRepository)
            collectionJob.cancel()
        }

    @Test
    fun `onDetachTelemetryLogで添付を解除する`() =
        runTest {
            val log = telemetryLog(id = 1L)
            every { telemetryLogRepository.observeTelemetryLogDetail(1L) } returns
                flowOf(TelemetryLogDetail(current = log, previous = null))
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            viewModel.setTelemetryLogId(1L)
            viewModel.uiState.first { it.attachedTelemetryLog != null }

            viewModel.onDetachTelemetryLog()

            assertNull(viewModel.uiState.first { it.attachedTelemetryLog == null }.attachedTelemetryLog)
            verify(exactly = 1) { telemetryLogRepository.observeTelemetryLogDetail(1L) }
            confirmVerified(telemetryLogRepository)
            collectionJob.cancel()
        }

    @Test
    fun `添付したテレメトリログの情報を含めて送信する`() =
        runTest {
            val log = telemetryLog(id = 1L, telemetryJson = """{"lapCount":1}""")
            every { telemetryLogRepository.observeTelemetryLogDetail(1L) } returns
                flowOf(TelemetryLogDetail(current = log, previous = null))
            coEvery { repository.send(any()) } returns Result.success(Unit)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            viewModel.setTelemetryLogId(1L)
            viewModel.uiState.first { it.attachedTelemetryLog != null }

            viewModel.onMessageChanged("添付します")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()

            assertTrue(viewModel.uiState.value.isSent)
            coVerify(exactly = 1) {
                repository.send(
                    Feedback(
                        type = FeedbackType.BugReport,
                        message = "添付します",
                        name = "Kurou",
                        email = "user@example.com",
                        includesDiagnostics = true,
                        telemetryLogId = 1L,
                        telemetryLogJson = """{"lapCount":1}""",
                    ),
                )
            }
            verify(exactly = 1) { telemetryLogRepository.observeTelemetryLogDetail(1L) }
            confirmVerified(repository, telemetryLogRepository)
            collectionJob.cancel()
        }

    @Test
    fun `テレメトリログを添付して送信に成功したら添付が解除される`() =
        runTest {
            val log = telemetryLog(id = 1L, telemetryJson = """{"lapCount":1}""")
            every { telemetryLogRepository.observeTelemetryLogDetail(1L) } returns
                flowOf(TelemetryLogDetail(current = log, previous = null))
            val feedback =
                Feedback(
                    type = FeedbackType.BugReport,
                    message = "添付します",
                    name = "Kurou",
                    email = "user@example.com",
                    includesDiagnostics = true,
                    telemetryLogId = 1L,
                    telemetryLogJson = """{"lapCount":1}""",
                )
            coEvery { repository.send(feedback) } returns Result.success(Unit)
            val viewModel = createViewModel()
            val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
            viewModel.setTelemetryLogId(1L)
            viewModel.uiState.first { it.attachedTelemetryLog != null }

            viewModel.onMessageChanged("添付します")
            viewModel.onNameChanged("Kurou")
            viewModel.onEmailChanged("user@example.com")
            viewModel.onSend()

            val sentState = viewModel.uiState.first { it.isSent && it.attachedTelemetryLog == null }
            assertTrue(sentState.isSent)
            assertNull(sentState.attachedTelemetryLog)
            coVerify(exactly = 1) { repository.send(feedback) }
            verify(exactly = 1) { telemetryLogRepository.observeTelemetryLogDetail(1L) }
            confirmVerified(repository, telemetryLogRepository)
            collectionJob.cancel()
        }
}
