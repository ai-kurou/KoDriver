package kurou.kodriver

import android.app.Application
import io.sentry.android.core.SentryAndroid
import kurou.kodriver.data.androidDataModule
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin

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
            modules(listOf(androidDataModule(this@KoDriverApplication)) + appModules)
        }
    }
}
