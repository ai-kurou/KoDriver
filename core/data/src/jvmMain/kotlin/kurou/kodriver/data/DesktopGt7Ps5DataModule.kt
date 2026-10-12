package kurou.kodriver.data

import kurou.kodriver.data.preferences.createDebugStateCardOrderPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.data.preferences.createGt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Desktop 版の Gt7Ps5 設定系バインド（LongMethod 対策）。
 */
internal fun desktopDataModuleGt7Ps5(): Module =
    module {
        single<DebugStateCardOrderPreferencesRepository>(named(Simulator.Gt7Ps5.id)) {
            createDebugStateCardOrderPreferencesRepository(kodriverDirectory, Simulator.Gt7Ps5)
        }
        single<Gt7Ps5RemainingFuelLapsPreferencesRepository> {
            createGt7Ps5RemainingFuelLapsPreferencesRepository(kodriverDirectory)
        }
        single<Gt7Ps5RemainingFuelPreferencesRepository> {
            createGt7Ps5RemainingFuelPreferencesRepository(kodriverDirectory)
        }
        single<Gt7Ps5TyreTemperaturePreferencesRepository> {
            createGt7Ps5TyreTemperaturePreferencesRepository(kodriverDirectory)
        }
        single<Gt7Ps5MyBestLapPreferencesRepository> {
            createGt7Ps5MyBestLapPreferencesRepository(directory = kodriverDirectory)
        }
    }
