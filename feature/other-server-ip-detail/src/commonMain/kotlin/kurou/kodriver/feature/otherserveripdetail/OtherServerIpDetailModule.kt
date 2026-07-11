package kurou.kodriver.feature.otherserveripdetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val otherServerIpDetailModule = module {
    viewModelOf(::OtherServerIpDetailViewModel)
    factory<ServerConnectivityChecker> { createServerConnectivityChecker() }
}
