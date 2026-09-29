---
name: start-implementation
description: 実装内容とベースブランチを指定し、専用ワークツリーで実装からPR作成まで行う。
---

# start-implementation

このSkillが呼び出されたら、作業開始前にリポジトリルートの `.claude/commands/start-implementation.md` を全文読み、その定義を唯一の実装フローとして従うこと。そこに参照される `CLAUDE.md`、`docs/`、Gitワークフローのルールも読む。

Codexでは `$start-implementation <実装内容> [ベースブランチ] [true|false]` の形式で呼び出す。引数の解釈・検証は共通定義に従う。

参照先にCodex環境で利用できないClaude固有ツール（例: `ScheduleWakeup`、`PushNotification`）が記載されている場合は、それを実行したと偽らない。Codexで同等の機能を使えなければその制約をユーザーに伝え、他の手順は続行する。
