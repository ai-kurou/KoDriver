# .claude/skills/

KoDriverリポジトリ内で利用するClaude Code向けSkillを配置するディレクトリ。

## 収録しているSkill

以下のSkillは [chrisbanes/skills](https://github.com/chrisbanes/skills)（Apache License 2.0）から取り込み、日本語に翻訳したもの。KotlinおよびJetpack Composeの実装規約（`CLAUDE.md`）と方向性が一致する2件を選定した。ライセンス全文は各ディレクトリの `LICENSE` を参照。

- `compose-state-and-effects/` — Compose の状態所有権・Effectライフサイクル設計の指針。KoDriverのViewModel設計規約（`uiState` への集約、`stateIn`/`combine` による宣言的な状態組み立て）と親和性が高い。
- `kotlin-concurrency-and-flow/` — コルーチンの所有権・キャンセル・Flowの状態/イベントモデリングの指針。KoDriverのコルーチンエラーハンドリング規約（`runCatching` 禁止、`CancellationException` の再スロー）と親和性が高い。

改善案メモ（`docs/improvement-ideas.md`）での調査・検討を経て導入した。
