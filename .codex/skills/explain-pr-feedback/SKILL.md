---
name: explain-pr-feedback
description: PRレビュー・CI/Botの指摘を調査して妥当性と修正要否を報告する。修正や返信はしない。
---

# explain-pr-feedback

このSkillが呼び出されたら、作業開始前にリポジトリルートの `.claude/commands/explain-pr-feedback.md` を全文読み、その定義を唯一の手順として従うこと。そこに参照される `CLAUDE.md`、`docs/`、テンプレートも読む。引数の解釈、調査手順、変更の可否、安全制約は参照先に従い、省略・独自解釈しない。

Codexからは `$$explain-pr-feedback <引数>` の形式で呼び出す。PR番号など引数を省略した場合の扱いも参照先に従う。

参照先にCodex環境で利用できないClaude固有ツールが記載されている場合は、それを実行したと偽らない。Codexで同等の機能を使えなければその制約をユーザーに伝え、他の手順は続行する。
