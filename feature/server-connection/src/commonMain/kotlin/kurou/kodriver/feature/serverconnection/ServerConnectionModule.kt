package kurou.kodriver.feature.serverconnection

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val serverConnectionModule = module {
    viewModel { ServerConnectionViewModel(get(), get(), get(), get(named("appVersion"))) }
}
