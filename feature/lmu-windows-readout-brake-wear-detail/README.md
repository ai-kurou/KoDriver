# lmu-windows-readout-brake-wear-detail

LMU のブレーキ摩耗アナウンスの詳細設定画面。有効スイッチ、読み上げ文言（`{percent}` で閾値を埋め込み、試聴可）、車両クラス別の残量閾値スライダーを持つ。

残量の表示は DebugStateDetail のブレーキ残量カード（`BRAKE_WEAR`）で行う。警告の閾値に使う残量%は、観測した最大の厚さを新品時の厚さとみなし、車両クラス別の固定の破損厚さ（`:core:domain` の `LmuWindowsBrakeFailureThicknessDefaults.kt`）までの間の割合で計算する。

読み上げ判定は `:feature:lmu-windows-narrator` の `determineBrakeWearLow` が行う（いずれかの輪の残量が閾値以下になった時点で1回、全輪が閾値を超えるまで再読み上げしない）。設定は `:core:data` の `LmuWindowsVehicleClassBrakeWearPreferences` に保存する。エンドポイント仕様は [`docs/lmu-windows-rest-api.md`](../../docs/lmu-windows-rest-api.md) を参照。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-brake-wear-detail.svg)
<!-- MODULE-GRAPH-END -->
