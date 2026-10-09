"""承認依頼や未完了を成功扱いしないことを、CLIを実行せず検証する。"""

import copy
import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from nightly_todo import ALLOWED_TOOLS, WEEKDAYS, run_batch, select_tasks, validate_result

DOCUMENT = Path('docs/nightly-todo-list.md').read_text()


class NightlyTodoTest(unittest.TestCase):
    def setUp(self):
        self.tasks = select_tasks(DOCUMENT, 'Friday')
        self.payload = {
            'type': 'result', 'subtype': 'success', 'is_error': False,
            'permission_denials': [],
            'structured_output': {
                'checks': {key: {'status': 'completed', 'details': '確認済み。改善不要のため起票なし。'}
                           for key in self.tasks},
                'summary': '対象を確認しました。該当記事なし。', 'issue_urls': [],
            },
        }

    def test_weekday_selection(self):
        """毎晩項目を維持し、当日のローテーションだけを選ぶ。"""
        for weekday, japanese in WEEKDAYS.items():
            with self.subTest(weekday=weekday):
                tasks = select_tasks(DOCUMENT, weekday)
                expected = next(line[5:] for line in DOCUMENT.splitlines()
                                if line.startswith(f'- {japanese}: ')).split(' / ')
                self.assertEqual(expected, [value for key, value in tasks.items()
                                            if key.startswith('rotation-')])
                self.assertEqual(6, len([key for key in tasks if key.startswith('daily-')]))

    def test_invalid_task_document_fails(self):
        """対象が消えた文書を、調査完了として扱わない。"""
        for document in ['', '## 毎晩実行する項目\n\n## 曜日ローテーション項目\n- 金: 確認']:
            with self.subTest(document=document), tempfile.TemporaryDirectory() as tmp:
                self.assertEqual(1, run_batch(document, '2026-10-09', 'Friday', Path(tmp)))
                self.assertIn('未完了', (Path(tmp) / 'summary.md').read_text())

    def test_complete_with_zero_or_one_issue(self):
        """起票ゼロも完了として扱い、実際の起票URLも保持する。"""
        for urls in [[], ['https://github.com/ai-kurou/KoDriver/issues/2020']]:
            with self.subTest(urls=urls):
                self.payload['structured_output']['issue_urls'] = urls
                self.assertEqual(urls, validate_result(self.payload, self.tasks, 0)['issue_urls'])

    def test_incomplete_results_fail(self):
        """CLI成功でも拒否・結果欠落・未調査・不正結果は失敗させる。"""
        variants = []
        for key, value in [('permission_denials', [{'tool_name': 'Bash'}]),
                           ('is_error', True), ('subtype', 'error_max_turns'),
                           ('structured_output', None), ('type', 'assistant')]:
            payload = copy.deepcopy(self.payload)
            payload[key] = value
            variants.append(payload)
        for field, value in [('checks', {}), ('summary', ''), ('issue_urls', 'not a list'),
                             ('issue_urls', ['https://example.com/1']),
                             ('issue_urls', ['https://github.com/ai-kurou/KoDriver/issues/1'] * 2)]:
            payload = copy.deepcopy(self.payload)
            payload['structured_output'][field] = value
            variants.append(payload)
        for value in [{'status': 'blocked', 'details': '承認が必要'},
                      {'status': 'completed', 'details': ''}, None]:
            payload = copy.deepcopy(self.payload)
            payload['structured_output']['checks']['daily-1'] = value
            variants.append(payload)
        variants += [[], None]
        for payload in variants:
            with self.subTest(payload=payload), self.assertRaises(ValueError):
                validate_result(payload, self.tasks, 0)
        with self.assertRaises(ValueError):
            validate_result(self.payload, self.tasks, 1)

    def test_runner_retains_diagnostics(self):
        """承認依頼の平文、異常終了、権限拒否、正常完了を保存・区別する。"""
        for stdout_text, exit_code, expected in [
            ('This command needs your approval', 0, 1),
            (json.dumps(self.payload), 1, 1),
            (json.dumps(dict(self.payload, permission_denials=[{'tool_name': 'Bash'}])), 0, 1),
            (json.dumps(self.payload), 0, 0),
        ]:
            def fake_run(command, stdout, stderr, check):
                self.assertIn('--json-schema', command)
                self.assertIn('dontAsk', command)
                self.assertIn('Bash(gh issue view:*)', ALLOWED_TOOLS)
                stdout.write(stdout_text)
                stderr.write('診断用stderr')
                return subprocess.CompletedProcess(command, exit_code)

            with self.subTest(exit_code=exit_code, expected=expected), tempfile.TemporaryDirectory() as tmp:
                directory = Path(tmp)
                with patch('nightly_todo.subprocess.run', side_effect=fake_run):
                    self.assertEqual(expected, run_batch(DOCUMENT, '2026-10-09', 'Friday', directory))
                self.assertEqual(stdout_text, (directory / 'response.json').read_text())
                self.assertEqual('診断用stderr', (directory / 'stderr.log').read_text())
                self.assertEqual(str(exit_code), (directory / 'exit-code.txt').read_text())
                self.assertIn('未完了' if expected else '起票件数: 0',
                              (directory / 'summary.md').read_text())

    def test_missing_cli_fails_with_summary(self):
        """CLIが起動できなくても未完了サマリーを残す。"""
        with tempfile.TemporaryDirectory() as tmp, patch(
            'nightly_todo.subprocess.run', side_effect=FileNotFoundError('claude not found')
        ):
            self.assertEqual(1, run_batch(DOCUMENT, '2026-10-09', 'Friday', Path(tmp)))
            self.assertIn('claude not found', (Path(tmp) / 'summary.md').read_text())


if __name__ == '__main__':
    unittest.main()
