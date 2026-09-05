#!/usr/bin/env python3
"""guard_destructive_bash.py のユニットテスト（標準ライブラリの unittest のみを使用）。"""

import json
import subprocess
import sys
import unittest
from pathlib import Path

SCRIPT_PATH = Path(__file__).parent / "guard_destructive_bash.py"


def run_hook(tool_name: str, command: str | None) -> tuple[int, dict]:
    tool_input = {} if command is None else {"command": command}
    payload = {"tool_name": tool_name, "tool_input": tool_input}
    result = subprocess.run(
        [sys.executable, str(SCRIPT_PATH)],
        input=json.dumps(payload),
        capture_output=True,
        text=True,
        check=False,
    )
    stdout = result.stdout.strip()
    parsed = json.loads(stdout) if stdout else {}
    return result.returncode, parsed


class GuardDestructiveBashTest(unittest.TestCase):
    def test_非Bashツールは何も出力しない(self) -> None:
        returncode, output = run_hook("Read", None)
        self.assertEqual(0, returncode)
        self.assertEqual({}, output)

    def test_rmを含まないコマンドは何も出力しない(self) -> None:
        returncode, output = run_hook("Bash", "ls -la")
        self.assertEqual(0, returncode)
        self.assertEqual({}, output)

    def test_ルート直下への再帰削除はdenyになる(self) -> None:
        returncode, output = run_hook("Bash", "rm -rf /")
        self.assertEqual(0, returncode)
        self.assertEqual(
            "deny", output["hookSpecificOutput"]["permissionDecision"]
        )

    def test_ホームディレクトリへの再帰削除はdenyになる(self) -> None:
        returncode, output = run_hook("Bash", "rm -rf ~")
        self.assertEqual(0, returncode)
        self.assertEqual(
            "deny", output["hookSpecificOutput"]["permissionDecision"]
        )

    def test_ssh鍵ディレクトリへの削除はdenyになる(self) -> None:
        returncode, output = run_hook("Bash", "rm -rf ~/.ssh")
        self.assertEqual(0, returncode)
        self.assertEqual(
            "deny", output["hookSpecificOutput"]["permissionDecision"]
        )

    def test_保護対象外への再帰強制削除はaskになる(self) -> None:
        returncode, output = run_hook("Bash", "rm -rf ./build")
        self.assertEqual(0, returncode)
        self.assertEqual(
            "ask", output["hookSpecificOutput"]["permissionDecision"]
        )

    def test_再帰も強制も伴わない通常のrmは何も出力しない(self) -> None:
        returncode, output = run_hook("Bash", "rm ./tmp.txt")
        self.assertEqual(0, returncode)
        self.assertEqual({}, output)

    def test_複数コマンドを連結した中に危険なrmがあればdenyになる(self) -> None:
        returncode, output = run_hook("Bash", "echo hi && rm -rf /")
        self.assertEqual(0, returncode)
        self.assertEqual(
            "deny", output["hookSpecificOutput"]["permissionDecision"]
        )

    def test_commandが空文字の場合は何も出力しない(self) -> None:
        returncode, output = run_hook("Bash", "")
        self.assertEqual(0, returncode)
        self.assertEqual({}, output)

    def test_不正なJSON入力の場合は何も出力しない(self) -> None:
        result = subprocess.run(
            [sys.executable, str(SCRIPT_PATH)],
            input="not a json",
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(0, result.returncode)
        self.assertEqual("", result.stdout.strip())


if __name__ == "__main__":
    unittest.main()
