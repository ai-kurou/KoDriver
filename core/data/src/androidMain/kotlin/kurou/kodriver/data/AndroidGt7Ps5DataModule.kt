package kurou.kodriver.data

import android.content.Context
import kurou.kodriver.data.preferences.createGt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 版の Gt7Ps5 設定系バインド（LongMethod 対策）。
 */
internal fun androidDataModuleGt7Ps5(context: Context): Module =
    module {
        single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
            createGt7Ps5RemainingFuelLapsPreferencesRepository(context.filesDir.absolutePath)
        }
        single<Gt7Ps5RemainingFuelPreferencesRepository> {
            createGt7Ps5RemainingFuelPreferencesRepository(context.filesDir.absolutePath)
        }
        single<Gt7Ps5TyreTemperaturePreferencesRepository> {
            createGt7Ps5TyreTemperaturePreferencesRepository(context.filesDir.absolutePath)
        }
        single<Gt7Ps5MyBestLapPreferencesRepository> {
            createGt7Ps5MyBestLapPreferencesRepository(context.filesDir.absolutePath)
        }
    }
