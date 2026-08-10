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

---

## 設計・重複

- **対象**: `ReadoutItemKey`（`core:domain`）と各 `Determine*NarratorReadoutUseCase`
  **課題**: `ReadoutItemKey` の配線漏れ（#464, #472）は目視確認に頼っており、機械的な検証手段がない。
  **改善案**: `ReadoutItemKey` の全定義を列挙し、対応する `Determine{LmuWindows,Gt7Ps5,AceWindows}NarratorReadoutUseCase` のソース内にキーが出現することを検証するテストを追加し、将来の配線漏れを自動検出できるようにする。

- **対象**: `Determine{LmuWindows,Gt7Ps5,AceWindows}NarratorReadoutUseCase`
  **課題**: 3シミュレータの `Determine*NarratorReadoutUseCase` はいずれも「Root無効なら空リスト、有効ならサブキー判定でSpeechEventをマッピング」という同型パターンを個別に実装しており、ロジックが重複している可能性が高い。
  **改善案**: シミュレータ間の仕様差（LMUのしきい値ベース判定など）により完全な共通化は難しいが、Root無効時のガード処理や `ReadoutItemKey.Flag.X to SpeechEvent.X` のようなペア列挙を汎用ヘルパー関数化できないか検討する。

