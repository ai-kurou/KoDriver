package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.lifecycle.viewModelScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5DebugStateDetailModuleTest {
    private val gt7Ps5Repository: Gt7Ps5Repository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModelとすべてのUseCaseをRepositoryから解決できる`() {
        every { gt7Ps5Repository.telemetryStream() } returns emptyFlow()
        val koin =
            koinApplication {
                modules(
                    gt7Ps5DebugStateDetailModule,
                    module {
                        single<Gt7Ps5Repository> { gt7Ps5Repository }
                    },
                )
            }.koin
        try {
            assertNotNull(koin.get<Gt7Ps5DebugStateUseCases>())
            assertNotNull(koin.get<ObserveGt7Ps5UseCase>())
            assertNotNull(koin.get<ObserveGt7Ps5VehicleClassUseCase>())
            val viewModel = koin.get<Gt7Ps5DebugStateDetailViewModel>()
            try {
                assertNotNull(viewModel)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        } finally {
            koin.close()
        }
    }
}
