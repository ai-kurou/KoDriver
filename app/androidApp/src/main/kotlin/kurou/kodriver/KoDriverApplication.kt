package kurou.kodriver

import android.app.Application
import android.provider.Settings
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.data.androidDataModule
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
            options.environment = "android"
        }
        Sentry.setUser(
            User().apply {
                id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            },
        )
        startKoin {
            // composition root: データ層モジュール（:core:*data）＋ 全 feature の appModules ＋
            // アプリバージョン定数（named("appVersion")。server-connection 等が get で解決）を束ねる。
            modules(
                listOf(
                    androidDataModule(this@KoDriverApplication),
                    gt7Ps5DataModule,
                ) +
                    appModules +
                    listOf(module { single(named("appVersion")) { BuildConfig.VERSION_NAME } }),
            )
        }
    }
}
