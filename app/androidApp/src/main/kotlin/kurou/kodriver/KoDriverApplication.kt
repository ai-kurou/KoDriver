package kurou.kodriver

import android.app.Application
import kurou.kodriver.data.androidDataModule
import kurou.kodriver.presentation.appModules
import org.koin.core.context.startKoin

class KoDriverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(listOf(androidDataModule(this@KoDriverApplication)) + appModules)
        }
    }
}
