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

## readout-list / Narrator

- **対象**: `core:domain`（`QueuePreferencesRepository`・`ObserveQueueEnabledStatesUseCase`・`SaveQueueEnabledStateUseCase`）・`core:data`（DataStore 実装）・`feature:readout-list`・`feature:lmu-windows-narrator` / `feature:gt7-ps5-narrator`
  **課題**: キュー追加トグル（listPane の `FilledIconToggleButton`）の ON/OFF 状態を永続化するための Repository・UseCase（core モジュールのみ）を実装した。まだ以下は未着手。
  1. `feature:readout-list` 側の配線（`ReadoutListViewModel`・`ReadoutListUiState`・`ReadoutListPane.kt`）。現状 `FilledIconToggleButton` は `remember { mutableStateOf(false) }` のローカル状態のままで、この Repository/UseCase を一切参照していない。
  2. 読み上げが重なった際に実際にキューへ積んで順番に読み上げる、というキュー機能本体の動作（`LmuWindowsNarratorViewModel` / `Gt7Ps5NarratorViewModel` とその判定ロジックでのゲート・キューイング処理）。
  **改善案**: 1 の配線と 2 のNarrator側ゲート処理を別作業として実装する。CLAUDE.md の「ReadoutItemKey の配線」の原則どおり、永続化・UI配線・実際のゲート処理の3つが揃って初めて機能するため、実装時はすべて確認すること（#464, #472 と同種の「スイッチはあるが効果がない」状態を避ける）。
