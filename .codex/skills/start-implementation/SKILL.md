---
name: start-implementation
description: Claude Codeのstart-implementation手順を共通定義から読み、Codexで実行する。
---

# start-implementation

このSkillが呼び出されたら、作業を始める前にリポジトリルートの `.claude/commands/start-implementation.md` を全文読み、その手順を唯一の実装フロー定義として従うこと。手順中に参照される `CLAUDE.md`、`docs/`、Gitワークフローのルールも必ず読む。

ユーザーが指定した引数・条件はSkill呼び出し時のメッセージから取得する。Codexでは `$start-implementation <実装内容> [ベースブランチ] [true|false]` の形式で利用する。引数の解釈・検証は共通定義に従う。

Codexの環境に存在しないClaude固有ツール（例: `ScheduleWakeup`、`PushNotification`）は使えない。該当箇所に到達したら、Codexで利用できる機能があるか確認し、なければ共通定義の目的を保てる範囲でCI状態をユーザーに伝え、完了報告で制約を説明する。ツールを実行したと偽らない。
