package kurou.kodriver.presentation

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuConnectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初回確認結果を接続状態へ反映する`() = runTest {
        val repository = FakeConnectionRepository(isConnected = true)
        val viewModel = LmuConnectionViewModel(CheckLmuConnectionUseCase(repository))
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }

        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isConnected)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `一定間隔で接続状態を更新する`() = runTest {
        val repository = FakeConnectionRepository(isConnected = false)
        val viewModel = LmuConnectionViewModel(CheckLmuConnectionUseCase(repository))
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.value.isConnected)

        repository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isConnected)
        collectionJob.cancelAndJoin()
    }

    @Test
    fun `接続確認で例外が発生しても未接続として監視を継続する`() = runTest {
        val repository = FakeConnectionRepository(
            isConnected = false,
            failureCount = 1,
        )
        val viewModel = LmuConnectionViewModel(CheckLmuConnectionUseCase(repository))
        val collectionJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.uiState.collect() }
        dispatcher.scheduler.runCurrent()
        assertFalse(viewModel.uiState.value.isConnected)

        repository.isConnected = true
        dispatcher.scheduler.advanceTimeBy(1_000L)
        dispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isConnected)
        collectionJob.cancelAndJoin()
    }
}

private class FakeConnectionRepository(
    var isConnected: Boolean,
    var failureCount: Int = 0,
) : LmuRepository {
    override fun telemetryStream(): Flow<LmuTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean {
        if (failureCount > 0) {
            failureCount--
            error("connection check failed")
        }
        return isConnected
    }
    override suspend fun disconnect() = Unit
}
