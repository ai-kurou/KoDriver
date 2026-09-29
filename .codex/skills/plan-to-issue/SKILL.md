---
name: plan-to-issue
description: コードを変更せず実装案を調査・計画し、GitHub Issueを作成または指定Issueを更新する。
---

# plan-to-issue

このSkillが呼び出されたら、作業開始前にリポジトリルートの `.claude/commands/plan-to-issue.md` を全文読み、その定義を唯一の手順として従うこと。そこに参照される `CLAUDE.md`、`docs/`、テンプレートも読む。引数の解釈、調査手順、変更の可否、安全制約は参照先に従い、省略・独自解釈しない。

Codexからは `$$plan-to-issue <引数>` の形式で呼び出す。PR番号など引数を省略した場合の扱いも参照先に従う。

参照先にCodex環境で利用できないClaude固有ツールが記載されている場合は、それを実行したと偽らない。Codexで同等の機能を使えなければその制約をユーザーに伝え、他の手順は続行する。
