# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  **課題**: <現状の問題・気になっている点>
  **改善案**: <どう変えたいか>
```

## 音声アセット

- **対象**: `feature/lmu-windows-narrator/src/commonMain/kotlin/kurou/kodriver/feature/lmuwindowsnarrator/LmuWindowsWavNarratorEngine.kt`（`SpeechEvent.PitTimingTyreWearWarning`）
  **課題**: ピットタイミングのタイヤ摩耗予想残り周回数読み上げ用のWAVアセット（`files/tyre_wear_laps_0.wav`〜`files/tyre_wear_laps_5.wav`）が未整備。バーチャルエナジー分は既存の`remaining_virtual_energy_laps_*.wav`を流用してマッピング済みだが、タイヤ摩耗分は`eventToFile`にマッピングしていないため、`SpeechEvent.PitTimingTyreWearWarning`が発生しても無音のまま（`sounds[event]`が存在せず`speak()`が何もしない）。
  **改善案**: 「タイヤは残り約N周」のようなWAVファイルを`files/tyre_wear_laps_0.wav`〜`_5.wav`として用意し、`LmuWindowsWavNarratorEngine.eventToFile`に`SpeechEvent.PitTimingTyreWearWarning(laps)`のマッピングを追加する（GT7の`remaining_fuel_laps_N.wav`をVE分に流用した`d100a7ee`のパターンを参照）。
