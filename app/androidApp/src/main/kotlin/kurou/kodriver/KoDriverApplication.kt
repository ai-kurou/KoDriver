package kurou.kodriver

import android.app.Application
import io.sentry.android.core.SentryAndroid
import kurou.kodriver.core.gt7ps5data.createGt7UdpPortPreferencesRepository
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.data.androidDataModule
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val SENTRY_DSN =
    "https://93dc09daf8552c39b0eea61b4f1319ee@o4511575800676352.ingest.us.sentry.io/4511575816667136"

class KoDriverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SentryAndroid.init(this) { options ->
            options.dsn = SENTRY_DSN
            options.isEnabled = !BuildConfig.DEBUG
        }
        startKoin {
            modules(
                listOf(
                    androidDataModule(this@KoDriverApplication),
                    gt7Ps5DataModule,
                    module {
                        single<Gt7UdpPortPreferencesRepository> {
                            createGt7UdpPortPreferencesRepository(filesDir.absolutePath)
                        }
                    },
                ) +
                    appModules +
                    listOf(module { single(named("appVersion")) { BuildConfig.VERSION_NAME } }),
            )
        }
    }
}
