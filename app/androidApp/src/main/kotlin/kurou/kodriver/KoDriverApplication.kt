package kurou.kodriver

import android.app.Application
import kurou.kodriver.data.androidDataModule
import kurou.kodriver.feature.readout.readoutModule
import org.koin.core.context.startKoin

class KoDriverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(androidDataModule(this@KoDriverApplication), readoutModule)
        }
    }
}
