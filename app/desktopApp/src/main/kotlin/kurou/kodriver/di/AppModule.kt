package kurou.kodriver.di

import kurou.kodriver.WindowsTts
import kurou.kodriver.data.repository.LmuRepositoryImpl
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.usecase.DisconnectLmuUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import kurou.kodriver.presentation.LmuViewModel
import kurou.kodriver.presentation.TtsEngine
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<LmuRepository> { LmuRepositoryImpl() }
    factory { ObserveLmuUseCase(get()) }
    factory { DisconnectLmuUseCase(get()) }
    single<TtsEngine> { TtsEngine { WindowsTts.speak(it) } }
    viewModel { LmuViewModel(get(), get(), get()).also { it.startObserving() } }
}
