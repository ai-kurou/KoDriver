# core:lmu-windows-domain

LMU（Le Mans Ultimate）固有のドメイン層（モデル・Repository インターフェース・UseCase）。

- `:core:domain` に依存する（共有型 `ReadoutItemKey` / `SpeechEvent` 等を参照するため）。
- LMU 固有の sealed key、`LMU_WINDOWS_READOUT_ITEM_KEYS` と登録 spec（`LmuWindowsReadoutItemKeys.kt`）、読み上げ既定値は本モジュールに置く。新しい key は必ずキー一覧にも追加する。
- パッケージ名は `kurou.kodriver.domain.lmuwindows` のまま（`:core:domain` と同一パッケージを分割して持つ）。

燃料の消費量を追跡し `fuelLapsTrackingState` に蓄積する。蓄積値から残り周回数を算出できるが、手動・自動の読み上げには未接続。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-lmu-windows-domain.svg)
<!-- MODULE-GRAPH-END -->
