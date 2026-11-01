package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class LmuWindowsPitTimingLapsUseCasesTest {
    @Test
    fun `周回数を監視するUseCaseはリポジトリの監視Flowを返す`() {
        assertObserves<LmuWindowsPitTimingPreferencesRepository, Int>(
            "ObserveLmuWindowsPitTimingFuelLapsUseCase",
            2,
            observe = { observeFuelLaps() },
            useCase = { ObserveLmuWindowsPitTimingFuelLapsUseCase(it)() },
        )
        assertObserves<LmuWindowsPitTimingPreferencesRepository, Int>(
            "ObserveLmuWindowsPitTimingTyreWearLapsUseCase",
            2,
            observe = { observeTyreWearLaps() },
            useCase = { ObserveLmuWindowsPitTimingTyreWearLapsUseCase(it)() },
        )
        assertObserves<LmuWindowsPitTimingPreferencesRepository, Int>(
            "ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase",
            5,
            observe = { observeVirtualEnergyLaps() },
            useCase = { ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase(it)() },
        )
    }

    @Test
    fun `周回数を保存するUseCaseはリポジトリの保存メソッドへ委譲する`() {
        assertSaves<LmuWindowsPitTimingPreferencesRepository>(
            "SaveLmuWindowsPitTimingFuelLapsUseCase",
            save = { saveFuelLaps(2) },
            useCase = { SaveLmuWindowsPitTimingFuelLapsUseCase(it)(2) },
        )
        assertSaves<LmuWindowsPitTimingPreferencesRepository>(
            "SaveLmuWindowsPitTimingTyreWearLapsUseCase",
            save = { saveTyreWearLaps(2) },
            useCase = { SaveLmuWindowsPitTimingTyreWearLapsUseCase(it)(2) },
        )
        assertSaves<LmuWindowsPitTimingPreferencesRepository>(
            "SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase",
            save = { saveVirtualEnergyLaps(1) },
            useCase = { SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(it)(1) },
        )
    }
}
