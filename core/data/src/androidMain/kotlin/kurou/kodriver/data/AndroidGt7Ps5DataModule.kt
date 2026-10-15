package kurou.kodriver.data

import android.content.Context
import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.data.preferences.createManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Android 版の Gt7Ps5 設定系バインド（LongMethod 対策）。
 */
internal fun androidDataModuleGt7Ps5(context: Context): Module =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createDebugStateCardOrderPreferencesRepository(context.filesDir.absolutePath, Simulator.Gt7Ps5)
        }
        single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createManualReadoutSetPreferencesRepository(context.filesDir.absolutePath, Simulator.Gt7Ps5)
        }
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
