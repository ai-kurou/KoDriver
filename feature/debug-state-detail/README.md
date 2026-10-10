# debug-state-detail

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-debug-state-detail.svg)
<!-- MODULE-GRAPH-END -->

## ブレーキ残量カード

ブレーキ温度カードの次に、LMU 専用のブレーキ残量カード（`BRAKE_WEAR`）を表示する。
FL / FR / RL / RR の残量（小数1桁の %）と厚さ（小数3桁の mm）を2列で表示し、欠損した輪は `--` とする。
Android・ACE・GT7、および LMU の取得失敗時は「取得できません」と表示する。
Android は既存の取得不可 Repository を利用するため、WebSocket にブレーキ残量の配信を追加しない。
保存済みのカード順序は維持し、新規カードは末尾に補完する。
