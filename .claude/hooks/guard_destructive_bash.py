#!/usr/bin/env python3
"""Claude Code の PreToolUse フック。Bashツールで実行されようとしている
`rm` 呼び出しを解析し、保護対象パスへの削除は deny、それ以外の `-r`/`-f` を
伴う削除は ask（一時停止して確認）にする。標準ライブラリのみを使用する。
"""

import json
import re
import sys

# シェル変数展開の失敗（未定義変数が空文字になる等）で意図せず全体を
# 削除してしまう典型的な保護対象パス。
PROTECTED_PATH_PATTERNS = (
    re.compile(r"^/+$"),
    re.compile(r"^/\*+$"),
    re.compile(r"^~/?$"),
    re.compile(r"^\$HOME/?$"),
    re.compile(r"^~/\.ssh/?.*$"),
    re.compile(r"^\$HOME/\.ssh/?.*$"),
    re.compile(r"^/(bin|boot|dev|etc|lib|proc|root|sbin|sys|usr|var|System|Library|Users)/?$"),
)

RECURSIVE_FORCE_FLAG_RE = re.compile(r"^-[a-zA-Z]*[rRf][a-zA-Z]*$")


def _split_command(command: str) -> list[str]:
    """クォート・エスケープの完全な解釈は行わず、空白区切りの簡易分割のみ行う。"""
    return command.split()


def _find_rm_invocations(command: str) -> list[list[str]]:
    """`;`, `&&`, `||`, `|` で連結された各サブコマンドから `rm` 呼び出しを抽出する。"""
    segments = re.split(r"&&|\|\||;|\|", command)
    invocations = []
    for segment in segments:
        tokens = _split_command(segment.strip())
        for index, token in enumerate(tokens):
            if token == "rm":
                invocations.append(tokens[index + 1 :])
    return invocations


def _is_recursive_force(args: list[str]) -> bool:
    for arg in args:
        if arg in ("--recursive", "--force"):
            return True
        if RECURSIVE_FORCE_FLAG_RE.match(arg):
            return True
    return False


def _is_protected_path(path: str) -> bool:
    normalized = path.rstrip("/") or "/"
    for pattern in PROTECTED_PATH_PATTERNS:
        if pattern.match(path) or pattern.match(normalized):
            return True
    return False


def _evaluate_rm_invocation(args: list[str]) -> str | None:
    """危険度に応じて 'deny' / 'ask' / None（安全）を返す。"""
    targets = [arg for arg in args if not arg.startswith("-")]
    if not targets:
        return None

    if any(_is_protected_path(target) for target in targets):
        return "deny"

    if _is_recursive_force(args):
        return "ask"

    return None


def main() -> int:
    raw_input = sys.stdin.read()
    try:
        payload = json.loads(raw_input) if raw_input else {}
    except json.JSONDecodeError:
        # 入力が壊れている場合は判定を諦め、通常の許可フローに委ねる。
        return 0

    if payload.get("tool_name") != "Bash":
        return 0

    command = payload.get("tool_input", {}).get("command", "")
    if not isinstance(command, str) or not command:
        return 0

    worst_decision = None
    worst_invocation: list[str] = []
    for invocation in _find_rm_invocations(command):
        decision = _evaluate_rm_invocation(invocation)
        if decision == "deny":
            worst_decision = "deny"
            worst_invocation = invocation
            break
        if decision == "ask" and worst_decision != "deny":
            worst_decision = "ask"
            worst_invocation = invocation

    if worst_decision is None:
        return 0

    reason = (
        "保護対象パスに対する rm の実行が検出されたため拒否しました: "
        f"rm {' '.join(worst_invocation)}"
        if worst_decision == "deny"
        else "再帰的/強制的な rm 呼び出しが検出されたため、実行前の確認を要求します: "
        f"rm {' '.join(worst_invocation)}"
    )

    output = {
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": worst_decision,
            "permissionDecisionReason": reason,
        }
    }
    print(json.dumps(output, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
