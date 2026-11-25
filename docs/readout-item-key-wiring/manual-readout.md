# 手動読み上げ・ホットキーの配線

[`../readout-item-key-wiring.md`](../readout-item-key-wiring.md) の詳細。手動読み上げセットのホットキー、手動専用項目のキー配置をまとめる。

## 手動読み上げセットのホットキー

セット詳細で保存した `ManualReadoutSet.hotkeyVirtualKeyCode` は、Windows の
`ManualReadoutHotkeyRepositoryImpl` が全シミュレーターの設定から観測して登録する。
押下は `ManualReadoutRequest(simulatorId, setId)` で配信し、LMU・GT7・ACE Narrator はタブからの依頼と
`merge` して同じ処理へ渡す。自分宛ての保存済みセットの項目と文言を参照し、手動読み上げの判定を使う。
セット詳細は同じ Repository の登録状態を観測し、競合・失敗時の再試行もセット識別子付きで委譲する。
Android は非対応実装を使い、キー未割り当て・非対応時は登録状態を表示しない。

### 共通ルール

- 対象は保存済みセットの項目のうち、シミュレーターごとの手動項目一覧（下表）に含まれるものだけとする。
- 最新値は受信から5秒以内（`NARRATOR_CURRENT_VALUE_MAX_AGE_MS`）のものだけを使う。未受信・受信から5秒超・非有限・範囲外は「データ無し」として扱う。
- データ無しの項目は「<項目名>はデータがありません」を保存順に「、」で連結する。各項目を個別に判定し、全項目がデータ無しでも項目ごとの文を連結する（先頭 `MANUAL_READOUT_SET_ITEM_MAX` 件まで）。
- 本文は保存順に項目の文言を並べ、空欄（空白のみの文言）を除外して「、」で連結した1イベントにまとめる。全項目の文言が空欄なら空文字とする。
- 文言はセットの `itemTexts` を使い、未保存なら手動専用の既定文言（下表の定数名）を使う。プレースホルダーは既存の `format…ReadoutText` で置換する。
- 対応項目がない場合は「読み上げる項目がありません」（`MANUAL_READOUT_NO_ITEMS_TEXT`）を読む。
- 自動読み上げのスイッチ・しきい値・重複抑止・周回内タイミング・警告済み状態（`remainingFuelWarned` 等）は変更しない。手動側は自動のスイッチや判定に従わない。
- 開始音・ログ・優先度のキーは、LMU は先頭の対応項目から決まるキー、GT7 と ACE は `RemainingFuel.Root` 固定（LMU の詳細は下の補足）。
- 1イベントは手動優先で読み上げ、同じ本文をログに保存する。

### LMU Windows（12項目）

キーは `LmuWindowsReadoutItemKey.` の接頭辞を省略し、`value` の値（`lmu_windows_` 接頭辞）を括弧内に示す。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| バーチャルエネルギー残量 | `RemainingVirtualEnergy.Root.value`（`lmu_windows_auto_remaining_virtual_energy`） | バーチャルエネルギーの残量比率（0〜1）を整数％ | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT`（「バーチャルエネルギーは残り{percent}%」） | 未受信・鮮度切れ | ― |
| 燃料残量 | `RemainingFuel.Root.value`（`lmu_windows_auto_remaining_fuel`） | 受信値 `remainingPercent` を比率（0〜1）に変換し整数％ | `LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}%」） | 未受信・鮮度切れ | 自動燃料警告の `remainingFuelWarned` は変更しない。 |
| タイヤ摩耗 | `TyreWear.Root.value`（`lmu_windows_auto_tyre_wear`） | 4輪の最小残存率から求めた整数％ | `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT`（「タイヤは残り{percent}%」） | 未受信・鮮度切れ | 「タイヤ摩耗の残り周回数」とは別項目。 |
| ブレーキ残量 | `BrakeWear.Root.value`（`lmu_windows_auto_brake_wear`） | 4輪 `remainingPercent`（0〜100）の最小値を四捨五入した整数％ | `LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT`（「ブレーキは残り{percent}%」） | 未受信・鮮度切れ・空の `wheels` | 自動警告の前回値・警告済み状態は変更しない。 |
| ブレーキ温度 | `BrakeTemperature.Root.value`（`lmu_windows_auto_brake_temperature`） | `LmuWindowsBrakeTemperatureData.wheels` の最高温度を四捨五入した整数℃ | `LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT`（「ブレーキ温度は{celsius}℃」） | 未受信・鮮度切れ・空の `wheels` | 自動過熱警告の前回値・警告済み状態は変更しない。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`lmu_windows_auto_tyre_temperature`） | `LmuWindowsTyreCarcassTemperatureData.wheels` の `CelsiusReading.value` の最大カーカス温度を四捨五入した整数℃ | `LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度は{celsius}℃」） | 未受信・鮮度切れ・空の `wheels` | 自動過熱・低温警告の前回値・警告済み状態は変更しない。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`lmu_windows_auto_my_best_lap`） | セッションの `timing.bestLapTimeMs`（ms）。「1分23秒456」形式に整形 | `LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下 | 自動の `MyBestLap.Root`・`DetailEnabled` スイッチとは独立。`determineMyBestLap`・`personalBestMs` の状態は変更しない。 |
| バーチャルエネルギーの残り周回数 | `PitTiming.VirtualEnergy.value`（`lmu_windows_auto_pit_timing_virtual_energy`） | 蓄積済みの `pitTimingVirtualEnergyTrackingState` から算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT`（「バーチャルエネルギーは残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT`（「バーチャルエネルギーは残り1周未満」） | 蓄積前（`currentLap == -1`）、消費量が null・0以下、算出結果が負、最新エネルギーが未受信・鮮度切れ | 算出式は下の補足。自動のスイッチ・閾値・ベストラップ経過時間には依存しない。追跡状態・自動判定状態（`lastAnnouncedPitTimingVirtualEnergyLaps` 等）は変更しない。 |
| タイヤ摩耗の残り周回数 | `PitTiming.TyreWear.value`（`lmu_windows_auto_pit_timing_tyre_wear`） | 蓄積済みの `pitTimingTyreWearTrackingState` から同じ式で算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT`（「タイヤは残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT`（「タイヤは残り1周未満」） | 蓄積前、消費量が null・0以下・非有限、算出結果が負、最新タイヤ摩耗が未受信・鮮度切れ | 「タイヤ摩耗」(%) 項目とも、バーチャルエネルギーの残り周回数とも別項目。 |
| 燃料残り周回数 | `RemainingFuel.Laps.value`（`lmu_windows_auto_remaining_fuel_laps`） | 蓄積済みの `fuelLapsTrackingState` から同じ式で算出した残り周回数（周） | `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周は `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT`（「燃料は残り1周未満」） | 蓄積前、消費量が null・0以下・非有限・算出不能、最新燃料が未受信・鮮度切れ | 自動側の燃料判定は変更しない。 |
| クラス内前後車とのギャップ | `ClassCarGap.Root.value`（`lmu_windows_auto_class_car_gap`） | クラス内前方車・後方車との差（秒）。前方の後に後方を読む | `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT`（「前方{ahead}秒、後方{behind}秒」）。先頭は「クラス内先頭です」（`..._TOP_TEXT_DEFAULT`）、最後尾は「クラス内最後尾です」（`..._LAST_TEXT_DEFAULT`）に置き換える | 先頭・最後尾でない側のギャップが null・非有限・負 | 先頭キーは `VehicleApproach.Root`（下の補足）。空欄テンプレートは空文字。 |

LMU の補足は次のとおり。

- 残り周回数（バーチャルエネルギー・タイヤ摩耗・燃料）は、現在値（`currentValue`）を `lastValidLapConsumption` で割り、整数へ切り捨てる。1,000周（`LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX`）を超える算出結果はデータ無しとする。ただし丸め誤差の範囲（1e-9 以内）の超過だけは1,000周に丸める。
- 0周の固定文言は表の定数を読む。空欄テンプレートなら0周でも空文字にする。1周以上は `formatLmuWindowsPitTimingReadoutText` で `{laps}` を置換する。
- 開始音・ログ・優先度のキーは先頭の対応項目から決まる。`PitTiming.VirtualEnergy`・`PitTiming.TyreWear` は `PitTiming.Root`、`RemainingFuel.Laps` は `RemainingFuel.Root`、`ClassCarGap.Root` は `VehicleApproach.Root` とする。それ以外は先頭項目自身の Root を使い、対応項目がない場合は `RemainingVirtualEnergy.Root` を使う。
- 比率から整数％への四捨五入は `determineManualReadout` に集約し、既存の `PERCENTAGE_SCALE` を使う。

### GT7 PS5（4項目）

キーは `Gt7Ps5ReadoutItemKey.` の接頭辞を省略し、`value` の値（`gt7_ps5_` 接頭辞）を括弧内に示す。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| 燃料残量 | `RemainingFuel.Root.value`（`gt7_ps5_auto_remaining_fuel`） | 最新テレメトリの `gasLevel / gasCapacity * 100` を四捨五入し0〜100に制限した整数％ | `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}%」） | 未受信・鮮度切れ・不正な燃料値は「燃料残量はデータがありません」 | 既存の燃料用フォーマット関数を再利用する。開始音・ログ・常時キュー追加のキーもこの項目。 |
| 燃料残り周回数 | `RemainingFuelLaps.Root.value`（`gt7_ps5_auto_remaining_fuel_laps`） | 燃料残り周回数の推定（周）。算出式は下の補足 | `GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）。0周以下は `GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT`（「燃料は残り1周未満」） | 未受信・鮮度切れは「燃料残り周回数はデータがありません」。開始燃料・開始周回が未設定、完走周回数が0以下、消費燃料が0以下は null | 自動側のしきい値・重複抑止・有効スイッチは適用しない。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`gt7_ps5_auto_tyre_temperature`） | 4輪の有限値の最高温度を `roundToInt()` した整数℃（`currentMaxTyreTemperatureCelsius`） | `GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度は{celsius}℃」） | 未受信・鮮度切れ・全輪が非有限または0（取得不能）は「タイヤ温度はデータがありません」 | 詳細画面のカテゴリはタイヤ、プレビューは90℃。自動のしきい値・ヒステリシス・スイッチ判定は適用しない。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`gt7_ps5_auto_my_best_lap`） | テレメトリの `bestLapTimeMs`（ms）（`currentBestLapTimeMs`）。既存の `Gt7Ps5MyBestLap` で整形 | `GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下（未確定）は「自己ベストラップはデータがありません」 | 詳細画面のカテゴリはタイム、プレビューは83456ms。自動の `determineMyBestLap` の状態（`personalBestMs` 等）は変更しない。 |

GT7 PS5 の補足は次のとおり。

- 対応項目は保存順に先頭 `MANUAL_READOUT_SET_ITEM_MAX`（3件）まで処理する。
- 燃料残り周回数は、開始燃料＋総給油量－現在燃料を、完走周回数＋`CURRENT_LAP_CONSUMPTION_WEIGHT`（0.9）で割った平均消費を使い、現在燃料÷平均消費を整数へ切り捨てる。最終受信から5秒以内だけ推定値を使う。テレメトリごとに `determineRemainingFuelLaps` が呼ばれ、有効スイッチの判定より前に燃料追跡が更新されるため、自動読み上げが無効でも手動用の追跡が続く。
- 空欄テンプレートなら、0周以下やデータ無しでも読まない。
- GT7 と LMU のキーはそれぞれ `gt7_ps5_`・`lmu_windows_` 接頭辞を持ち衝突しない。

### ACE Windows（4項目）

キーは `AceWindowsReadoutItemKey.` の接頭辞を省略し、`value` の値（`ace_windows_` 接頭辞）を括弧内に示す。手動項目の順は「燃料残量」「燃料残り周回数」「自己ベストラップ」「タイヤ温度」。

| 項目 | キー | データ源と単位 | 既定文言（定数名） | データなし・特殊条件 | 補足 |
| --- | --- | --- | --- | --- | --- |
| 燃料残量 | `RemainingFuel.Root.value`（`ace_windows_auto_remaining_fuel`） | `AceWindowsFuelData.remainingPercent`（0〜100）を四捨五入した整数％（`currentRemainingFuelPercent`） | `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT`（「燃料は残り{percent}%」） | 未受信・鮮度切れ・非有限・0未満・100超は「燃料残量はデータがありません」 | 既存の `formatAceWindowsRemainingFuelReadoutText` で置換する。`remainingFuelWarned` 等の判定状態は変更しない。 |
| 燃料残り周回数 | `RemainingFuelLaps.Root.value`（`ace_windows_auto_remaining_fuel_laps`） | `AceWindowsRemainingFuelLapsData.remainingLaps` の整数部（周）（`currentRemainingFuelLaps`） | `ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」） | 未受信・鮮度切れ・非有限・負値・1,000周（`ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX`）超は「燃料残り周回数はデータがありません」 | ちょうど0と1周未満の正数は整数部へ切り捨てて0周を返す。 |
| 自己ベストラップ | `MyBestLap.Root.value`（`ace_windows_auto_my_best_lap`） | 最新ベストラップ（ms）。5秒以内かつ正値のときだけ渡す | `ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT`（「自己ベストラップ {laptime}」） | 未受信・鮮度切れ・0以下はデータ無し | 既定文言を `formatAceWindowsMyBestLapReadoutText` で整形する。 |
| タイヤ温度 | `TyreTemperature.Root.value`（`ace_windows_auto_tyre_temperature`） | `AceWindowsTyreCarcassTemperatureData.wheels` の有限かつ0より大きい最高値を四捨五入した整数℃（`currentMaxTyreTemperatureCelsius`） | `ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT`（「タイヤ温度は{celsius}℃」） | 未受信・鮮度切れ・有効な温度なし・ACE以外への切り替え後は「タイヤ温度はデータがありません」 | 既存の `formatAceWindowsTyreTemperatureReadoutText` と `findUnknownAceWindowsTyreTemperatureReadoutPlaceholders` を使う。詳細画面のカテゴリはタイヤ、プレビューは90℃。自動過熱警告のしきい値・ヒステリシス・スイッチ判定は適用せず、警告状態も変更しない。 |
| 前後車ギャップ | `CarGap.Root.value`（`ace_windows_auto_car_gap`） | `AceWindowsGapData`（前車・後車との符号付き秒数）。5秒以内のものを `AceWindowsManualReadoutItem.gap` に渡す | `ACE_WINDOWS_MANUAL_READOUT_CAR_GAP_TEXT_DEFAULT`（「前方{ahead}秒、後方{behind}秒」） | 未受信・鮮度切れ・ACE以外への切り替え後は「前後車とのギャップはデータがありません」 | 手動専用で自動スイッチなし。`{ahead}`は単位なしの数値「1.5」、`{behind}`は「2.3」（絶対値・小数1桁）。0・非有限値は節ごと「前方車なし」「後方車なし」に置換し、節を含まない文言では「なし」に置換する。読み上げ時の開始音・ログは他のACE手動項目と同じ `RemainingFuel.Root`、試聴の開始音は `VehicleApproach.Root`。プレビューは1.5秒・2.3秒。 |

ACE Windows の補足は次のとおり。

- 対応項目は保存順に先頭 `MANUAL_READOUT_SET_ITEM_MAX`（3件）まで処理する。
- 手動値は鮮度判定済みの `AceWindowsManualReadoutItem(key, textTemplate, value)` で受け取る。シミュレーター切り替え時は最新値と受信時刻をクリアする。
- 全項目の文言が空白なら空文字とする。
- 一覧は既存の `AceWindowsReadoutItemKey.RemainingFuel.Root` を含むため、手動の「燃料残量」を選択できる。開始音・ログ・手動優先の読み上げには既存の `RemainingFuel.Root` を使う。

## 手動専用項目のキー配置

手動の「燃料残り周回数」は `LmuWindowsReadoutItemKey.RemainingFuel.Laps.value`（`lmu_windows_auto_remaining_fuel_laps`）に対応する。
`RemainingFuel` 配下の手動専用項目で、TopLevel・自動タブ・スイッチには追加しない。手動項目定義の末尾（タイヤ摩耗の残り周回数の後）に並ぶ。
`fuelLapsTrackingState` の蓄積値を `manualFuelRemainingLaps` で読み、1周以上は `formatLmuWindowsPitTimingReadoutText` で `{laps}` を置換する。
既定文言は `LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT`（「燃料は残り約{laps}周」）、0周は
`LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT`（「燃料は残り1周未満」）。空欄テンプレートなら0周でも空文字にする。
蓄積前・消費量が null／0以下／非有限・算出不能・最新燃料が未受信または受信から5秒超の場合はデータ無しとする。
鮮度は `latestFuelReceivedAtMs` で判定し、ピットタイミングの鮮度には依存しない。
複数項目では「燃料残り周回数はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `RemainingFuel.Root` を使う。編集画面はACE・GT7の燃料項目と同じ「燃料」カテゴリに配置し、既存の
`LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER` を挿入・検証に使う。項目一覧・ホットキーは共通の手動項目定義を参照する。
手動の燃料読み上げは自動警告の有効状態・評価履歴を変更しない。


LMUピットタイミングの燃料は `LmuWindowsReadoutItemKey.PitTiming.Fuel`（`lmu_windows_auto_pit_timing_fuel`）で保存する。
詳細のスイッチ・閾値（既定3周、1〜5周）・通常／切迫時文言は既存の PitTiming DataStore を共有し、
ProtoNumber 8〜10を追加する。既存フィールド1〜7は維持し、旧データでは燃料の既定値を使う。
`ObserveLmuWindowsPitTimingEnabledStatesUseCase` の既定値は有効で、Narratorのマージ済み有効状態と
`determinePitTimingFuel` が `PitTiming.Root`／`PitTiming.Fuel` を判定する。
燃料警告は `energySource == LmuWindowsEnergySource.Fuel`（P2/P2Elms/P3/GTE/Unknown）でのみ発火し、
VEクラス・無効設定でも燃料消費の追跡と評価周の更新を続ける。詳細の燃料設定はクラスに関わらず表示し、listPaneは変更しない。
`pitTimingJob` が `exactPercent / 100.0` を一度だけ追跡し、手動の燃料残り周回数と `fuelLapsTrackingState` を共有する。
VE・タイヤ・燃料の候補の最小周回数を選び、共通の `PitTimingLapGate` を通った候補だけ読み上げ履歴へ記録する。
給油周を消費量の推定から除外し、給油で警告履歴を再武装、周回巻き戻りで追跡・評価履歴を初期化する。
通常の既定文言は「燃料は残り約{laps}周」、0周以下は「燃料は残り1周未満」。保存文言の解決・開始音・優先度・キュー・ログは
既存の `PitTiming.Root` の流儀を使い、ログに燃料スナップショットと判定状態を含める。

手動の「クラス内前後車とのギャップ」は `LmuWindowsReadoutItemKey.ClassCarGap.Root.value`（`lmu_windows_auto_class_car_gap`）に対応する。前方車の後に後方車のギャップを1文で読み上げる1項目で、統合前の「前方車」「後方車」の2項目（`lmu_windows_auto_class_car_gap_ahead` / `_behind`）は廃止した。
TopLevel・自動タブ・スイッチ・`LmuWindowsReadoutDefaults.kt` の列挙には追加しない手動専用項目で、`LMU_WINDOWS_READOUT_ITEM_KEYS` の末尾（`MyBestLap.DetailEnabled` の後）と手動項目定義の末尾（燃料残り周回数の後）に並ぶ。
`ObserveLmuWindowsClassCarGapUseCase` の最新値（`LmuWindowsClassCarGapData`）を `latestClassCarGap` / `latestClassCarGapReceivedAtMs` に保持し、受信から5秒（`NARRATOR_CURRENT_VALUE_MAX_AGE_MS`）以内のものだけを項目へ渡す。未受信・鮮度切れはデータ無しとする。
周回遅れ・周回先行の車両も前後の対象に含む。トップ・最後尾は `gapAheadSeconds` / `gapBehindSeconds` ではなく `classPosition == 1` / `classPosition == classVehicleCount`（両方非 null）で判定する。
文言は1つのテンプレートで、`{ahead}`（`LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER`）が前方の秒数、`{behind}`（`LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER`）が後方の秒数に置き換わる（単位なしの小数1桁、例「1.5」）。既定文言は `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT`（「前方{ahead}秒、後方{behind}秒」、組は `LMU_WINDOWS_CLASS_CAR_GAP_PLACEHOLDERS`）。先頭（前方車なし）のときは前方の節「前方{ahead}秒」全体を「クラス内先頭です」（`..._TOP_TEXT_DEFAULT`）に、最後尾のときは後方の節「後方{behind}秒」全体を「クラス内最後尾です」（`..._LAST_TEXT_DEFAULT`）に置き換える。節を含まない文言で車なしのときは、プレースホルダー単体を「なし」（`LMU_WINDOWS_CLASS_CAR_GAP_NONE_VALUE_TEXT`）に置き換える。
前方の節は、先頭なら節全体を固定文言 `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT`（「クラス内先頭です」）に、それ以外は0以上の有限値の秒差を小数1桁に丸めた秒数（`formatClassCarGapSeconds`）に置き換える。
後方の節は、最後尾なら節全体を固定文言 `LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT`（「クラス内最後尾です」）に、それ以外は同様に秒数へ置き換える。
先頭・最後尾でない側の秒差が null・非有限・負値のときは、項目全体をデータ無しとする。空欄テンプレートなら（データがあれば）空文字を読む。
保存済みセットに残る旧キーは、読み出し時に `migrateLmuWindowsManualReadoutLegacyClassCarGap`（`ManualReadoutSimulatorSpec.migrateLegacySet`）で新キー1つへ置き換える。旧キーが最初に現れた位置に置き、前方・後方の両方があっても1つにまとめる。旧キーの文言は新しい文言へ変換できないため破棄し、新キーには既定文言を使う。
複数項目ではデータ無しの項目に「<項目名>はデータがありません」を連結する。
先頭項目なら開始音・優先度・ログ・キューに `VehicleApproach.Root` を使う。編集画面は既存の「タイム」カテゴリに配置し、`{ahead}` と `{behind}` を挿入・検証に使う。
