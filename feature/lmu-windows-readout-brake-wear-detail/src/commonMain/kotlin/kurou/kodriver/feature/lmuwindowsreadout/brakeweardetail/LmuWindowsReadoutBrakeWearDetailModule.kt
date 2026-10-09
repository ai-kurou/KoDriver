package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearInvestigationUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ブレーキ摩耗の調査用詳細画面（lmu-windows-readout-brake-wear-detail feature）の Koin モジュール。
 *
 * 提供: LmuWindowsReadoutBrakeWearDetailViewModel と ObserveLmuWindowsBrakeWearInvestigationUseCase。
 * 消費（get で解決）: LmuWindowsBrakeWearInvestigationRepository
 *   （デスクトップは :core:lmu-windows-rest-api-data、Android は :core:data の取得不可スタブ）。
 */
val lmuWindowsReadoutBrakeWearDetailModule =
    module {
        viewModel { LmuWindowsReadoutBrakeWearDetailViewModel(get()) }
        factoryOf(::ObserveLmuWindowsBrakeWearInvestigationUseCase)
    }
