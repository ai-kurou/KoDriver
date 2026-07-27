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

## モジュール構成

- **対象**: `core/ace-windows-data/src/main/kotlin/kurou/kodriver/core/acewindowsdata/datasource/{SharedMemoryReader.kt,Kernel32FileMapping.kt,WindowsSharedMemoryReader.kt}`、`core/lmu-windows-data` の同名ファイル群
  **課題**: `:core:ace-windows-data` は `:core:lmu-windows-data` の共有メモリ読み取りロジック（`SharedMemoryReader` インターフェース・`Kernel32FileMapping`（JNA）・`WindowsSharedMemoryReader`）をほぼそのままコピーして作成したため、SonarQubeの重複コード指摘（New Code の Duplicated Lines 17.3%、`WindowsSharedMemoryReader.kt` 84.7%・`Kernel32FileMapping.kt` 73.3%・`AceWindowsGraphicsSharedMemorySource.kt` 46.3%）が発生している（PR #781）。
  **改善案**: `SharedMemoryReader`/`Kernel32FileMapping`/`WindowsSharedMemoryReader` を新規共通モジュール（例: `:core:windows-shared-memory-data`）に切り出し、`:core:lmu-windows-data` と `:core:ace-windows-data` の両方がそれに依存する形にリファクタリングする。新規モジュール追加は `moduleGraphAssert` の `allowed` 配列変更を伴うため、着手前にユーザー確認が必要。

