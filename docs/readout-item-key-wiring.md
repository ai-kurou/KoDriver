# ReadoutItemKey の配線（listPane / detailPane と Narrator の読み上げ判定の一致）

共通の `ReadoutItemKey` インターフェースと `ReadoutItemKeyRegistry` は `:core:domain` に置く。各シミュレーターの sealed key、`*ReadoutItemKeys.kt` の順序付き一覧/spec、`*ReadoutDefaults.kt` は該当する simulator domain に置く。新しい key を追加するときは必ずその simulator の `*ReadoutItemKeys.kt` にも含める。アプリ起動時に `:core:data` の登録関数が3 simulator の一覧を登録する。


自動読み上げの保存キーとシミュレーター別DataStoreファイル名は、`lmu_windows_` / `ace_windows_` / `gt7_ps5_` の直後に `auto_` を置く。共通の一覧設定は Desktop では `auto_readout_preferences.pb`、Android では `auto_readout_preferences.preferences_pb` に保存する。`auto_` 導入時はユーザー了承のもと設定を意図的に初期化し、設定の旧値・旧ファイルの移行は行わない。テレメトリログは Room の 4→5 マイグレーションで旧キーを新キーへ変換して保持する。開始音・ホットキーなど自動読み上げ以外の設定ファイル名は変更しない。
`ReadoutItemKey` は、読み上げ一覧画面（listPane）のトップレベルの項目スイッチと、各機能の詳細画面（detailPane）内のサブトグルの両方で使われる共通のキー空間である。listPane のスイッチは「その項目を Narrator で読み上げるかどうか」に一致する仕様であり、detailPane のサブトグルは「その項目内のどのイベントを読み上げるか」を絞り込む仕様である。

`ReadoutItemKey` を新設・変更する際は、以下の両方を必ず確認すること。

1. listPane / detailPane のスイッチがどの `DataStore`（`AutoReadoutPreferencesRepository` か、各機能固有の Preferences Repository か）に保存されるか
2. その `ReadoutItemKey` が実際に Narrator の読み上げ判定（LMU: `LmuWindowsNarratorViewModel` の `enabledStates` マージ処理と `core/lmu-windows-domain/src/commonMain/kotlin/kurou/kodriver/domain/lmuwindows/usecase/` の `DetermineLmuWindowsVehicleReadoutUseCase.kt`・`DetermineLmuWindowsTyreReadoutUseCase.kt`・`DetermineLmuWindowsBrakeReadoutUseCase.kt`・`DetermineLmuWindowsFuelReadoutUseCase.kt`・`DetermineLmuWindowsVirtualEnergyReadoutUseCase.kt`・`DetermineLmuWindowsPitTimingReadoutUseCase.kt`・`DetermineLmuWindowsRaceFlagReadoutUseCase.kt`・`DetermineLmuWindowsMyBestLapReadoutUseCase.kt`、GT7: `Gt7Ps5NarratorViewModel` とその判定処理）で参照されているか

`ReadoutItemKey` は複数の独立した `DataStore` に同名キーとして存在しうるため、片方だけ実装してもう片方（Narrator側の実際のゲート処理）への配線を忘れると、スイッチが存在するのに効果がない死んだ実装になる。過去に以下のバグが発生している。

- #464: タイヤ温度・自己ベストラップのデフォルト無効状態が Narrator に未反映だった
- #472: listPane の `VehicleDamage` スイッチが当時の `DetermineLmuWindowsNarratorReadoutUseCase.determineVehicleDamage`（現在は責務別に分割済みで `DetermineLmuWindowsVehicleReadoutUseCase.determineVehicleDamage`）から参照されておらず、子項目 `Overheat` のみでゲートされていたため、`VehicleDamage` をOFFにしても過熱警告の読み上げが止まらなかった

新しい `ReadoutItemKey` を読み上げ判定ロジックに追加する場合は、対応する読み上げ判定 UseCase のテストに「その項目を無効にした場合は読み上げられない」ケースを必ず追加すること。

## デバッグ読み上げの例外

デバッグ状態詳細ペインの要求は `ReadoutItemKey.topLevelEntriesOf` の項目を使用する。ログと優先度・キュー判定のキーは通常の Narrator と同じ TopLevel で、項目スイッチと determine 系の判定を意図的に通さず入力文字列を読み上げる。この経路は設定スイッチの配線確認には使わない。通常テレメトリ経路のスイッチ動作は従来どおり確認する。

## 詳細

項目ごとの配線・既定文言・保存先は次のファイルに分けている。新しい項目を追加したら該当ファイルも更新する。

- [自動読み上げ項目の配線と動作](readout-item-key-wiring/auto-readout.md)（ACE / LMU / GT7 の項目表と各項目の動作）
- [手動読み上げ・ホットキーの配線](readout-item-key-wiring/manual-readout.md)（手動読み上げセットのホットキー、手動専用項目のキー配置）
