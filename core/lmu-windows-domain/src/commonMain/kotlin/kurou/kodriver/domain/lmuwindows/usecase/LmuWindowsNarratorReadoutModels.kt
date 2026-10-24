package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKeyMapSerializer

/**
 * LMU 向け読み上げ判定の継続状態。
 *
 * フラッグ、車両接近、自己ベスト、温度・摩耗・ピットタイミングなどの重複読み上げを避けるため、
 * 前回までの入力と判定結果を保持する。
 * 自己ベストの状態は周回数の減少による新セッション検出時にリセットする。
 */
@Serializable
data class LmuWindowsNarratorState(
    val vehicleApproachState: LmuWindowsVehicleApproachState = LmuWindowsVehicleApproachState(),
    val previousRaceFlags: LmuWindowsRaceFlagsData? = null,
    val previousVehicleDamage: LmuWindowsVehicleDamageData? = null,
    val previousTyreDetached: LmuWindowsTyreDetachedData? = null,
    val personalBestMs: Long = Long.MAX_VALUE,
    val previousBestLapTimeMs: Long? = null,
    val previousLapCount: Int? = null,
    val tyreOverheating: Boolean = false,
    val tyreWearWarned: Boolean = false,
    val brakeOverheating: Boolean = false,
    val brakeWearWarned: Boolean = false,
    val previousGamePhaseForTyreLowWarning: SessionPhase? = null,
    val remainingVirtualEnergyWarned: Boolean = false,
    val remainingFuelWarned: Boolean = false,
    val lastAnnouncedPitTimingVirtualEnergyLaps: Int = -1,
    val lastPitTimingVirtualEnergyEvaluationLap: Int = -1,
    val pitTimingVirtualEnergyTrackingState: LmuWindowsPitTimingTrackingState = LmuWindowsPitTimingTrackingState(),
    val lastAnnouncedPitTimingTyreWearLaps: Int = -1,
    val lastPitTimingTyreWearEvaluationLap: Int = -1,
    val pitTimingTyreWearTrackingState: LmuWindowsPitTimingTrackingState = LmuWindowsPitTimingTrackingState(),
)

/**
 * ピットタイミング（バーチャルエネルギー・タイヤ摩耗の予想残り周回数）の推定に使う追跡状態。
 * 直近に完走した（バーチャルエネルギー補充・タイヤ交換なしの）ラップの消費量を、次回以降の推定基準として使う
 * 直近完走ラップ基準の方式。
 */
@Serializable
data class LmuWindowsPitTimingTrackingState(
    val session: Int? = null,
    val currentLap: Int = -1,
    val currentLapStartedAtMs: Long = 0L,
    val currentLapStartValue: Double = 0.0,
    val currentLapHasRefilled: Boolean = false,
    /** 周回境界から計測を開始した周だけを消費量の推定に使う。 */
    val currentLapStartedAtBoundary: Boolean = false,
    val currentValue: Double = 0.0,
    /** 閾値未満の増加を累積してバーチャルエネルギー補充・タイヤ交換を検出するための基準値。 */
    val refillBaselineValue: Double = 0.0,
    /** 直近に完走した（バーチャルエネルギー補充・タイヤ交換なしの）ラップの消費量。まだ存在しなければ null。 */
    val lastValidLapConsumption: Double? = null,
    val bestLapTimeMs: Long = -1L,
    val hasRefilled: Boolean = false,
    val isNewSession: Boolean = false,
    val observedAtMs: Long = 0L,
)

/** LMU の車両接近読み上げで、左右それぞれの接近継続状態を保持する。 */
@Serializable
data class LmuWindowsVehicleApproachState(
    val left: Map<Int, LmuWindowsApproachState> = emptyMap(),
    val right: Map<Int, LmuWindowsApproachState> = emptyMap(),
)

/** 1 台の周辺車両に対する接近開始時刻と読み上げ済み状態。 */
@Serializable
data class LmuWindowsApproachState(
    val startedAtMs: Long,
    val announced: Boolean,
    val sustainedAnnounced: Boolean = false,
)

internal data class ApproachSideStatesResult(
    val states: Map<Int, LmuWindowsApproachState>,
    val announce: Boolean,
    val sustainedAnnounce: Boolean,
)

/** LMU 向け読み上げ判定で参照するユーザー設定と現在周回情報。 */
@Serializable
data class LmuWindowsNarratorReadoutSettings(
    @Serializable(with = ReadoutItemKeyMapSerializer::class)
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val currentLap: Int,
    val skipFirstLap: Boolean,
    val vehicleApproachSustainedApproachDurationSeconds: Int,
    val tyreTemperatureHighThresholdCelsius: Celsius,
    val tyreTemperatureLowWarningPhases: Set<SessionPhase>,
    val tyreWearThresholdPercentage: Int,
    val brakeTemperatureHighThresholdCelsius: Celsius,
    val brakeWearLowThresholdPercent: Int,
    val remainingVirtualEnergyThresholdPercentage: Int,
    val remainingFuelThresholdPercentage: Int,
    val energySource: LmuWindowsEnergySource,
    val pitTimingVirtualEnergyLapsThreshold: Int,
    val pitTimingTyreWearLapsThreshold: Int,
)

/** LMU 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class LmuWindowsNarratorReadoutDecision(
    val state: LmuWindowsNarratorState,
    val events: List<ReadoutTextEvent>,
)

/**
 * タイヤ温度読み上げ判定（過熱・低温警告）の入力データ。
 * [DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureOverheat] /
 * [DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureLow] とテレメトリログ記録の
 * 双方でこの型を共有し、判定に使う入力とログに記録する入力がずれないようにする。
 */
@Serializable
data class TyreTemperatureReadoutInput(
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData,
    val raceFlags: LmuWindowsRaceFlagsData,
)
