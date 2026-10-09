"""夜間調査の対象選定、Claudeの実行、完了検証と診断保存を行う。"""

import argparse
import json
import re
import subprocess
from pathlib import Path

WEEKDAYS = dict(zip(
    ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
    ['月', '火', '水', '木', '金', '土', '日'],
))
ALLOWED_TOOLS = (
    'Read,Grep,Glob,WebSearch,WebFetch,Bash(gh issue list:*),'
    'Bash(gh issue view:*),Bash(gh issue create:*),Bash(gh run list:*),'
    'Bash(gh api repos/*/actions/runs*)'
)


def select_tasks(document, weekday):
    """毎晩の箇条書きと指定曜日の各調査を安定したIDで返す。"""
    daily = document.split('## 毎晩実行する項目\n', 1)[1].split('\n## 曜日ローテーション項目', 1)[0]
    tasks = {f'daily-{i}': line[2:] for i, line in enumerate(
        (line for line in daily.splitlines() if re.match(r'^- .+?[:：] ', line)), 1)}
    if not tasks:
        raise ValueError('毎晩の調査対象がありません')
    prefix = f'- {WEEKDAYS[weekday]}: '
    rotation = next(line[len(prefix):] for line in document.splitlines() if line.startswith(prefix))
    tasks.update({f'rotation-{i}': text for i, text in enumerate(rotation.split(' / '), 1)})
    if not rotation.strip():
        raise ValueError('曜日の調査対象がありません')
    return tasks


def result_schema(tasks):
    check = {'type': 'object', 'additionalProperties': False,
             'properties': {'status': {'type': 'string', 'enum': ['completed', 'blocked']},
                            'details': {'type': 'string', 'minLength': 1}},
             'required': ['status', 'details']}
    return {'type': 'object', 'additionalProperties': False,
            'properties': {
                'checks': {'type': 'object', 'additionalProperties': False,
                           'properties': {key: check for key in tasks}, 'required': list(tasks)},
                'summary': {'type': 'string', 'minLength': 1},
                'issue_urls': {'type': 'array', 'items': {'type': 'string'}}},
            'required': ['checks', 'summary', 'issue_urls']}


def validate_result(payload, tasks, exit_code):
    """CLIの成功と、権限拒否なし・全対象の完了を独立に検証する。"""
    if exit_code != 0:
        raise ValueError(f'Claude CLI終了コード: {exit_code}')
    if not isinstance(payload, dict) or payload.get('type') != 'result':
        raise ValueError('CLIのresultオブジェクトがありません')
    if payload.get('is_error') is not False or payload.get('subtype') != 'success':
        raise ValueError('Claudeがエラーまたは未完了を報告しました')
    if payload.get('permission_denials'):
        raise ValueError('ツールの権限拒否が発生しました（response.jsonを参照）')
    report = payload.get('structured_output')
    if not isinstance(report, dict):
        raise ValueError('構造化された調査結果がありません')
    checks = report.get('checks')
    if not isinstance(checks, dict) or set(checks) != set(tasks):
        raise ValueError('調査対象の結果に不足・余分・曜日の不一致があります')
    for key, check in checks.items():
        if not isinstance(check, dict) or check.get('status') != 'completed':
            raise ValueError(f'調査未完了: {key}')
        if not isinstance(check.get('details'), str) or not check['details'].strip():
            raise ValueError(f'調査内容と判断理由がありません: {key}')
    if not isinstance(report.get('summary'), str) or not report['summary'].strip():
        raise ValueError('調査サマリーがありません')
    urls = report.get('issue_urls')
    if not isinstance(urls, list) or any(
        not isinstance(url, str) or not url.startswith('https://github.com/ai-kurou/KoDriver/issues/')
        or not url.rsplit('/', 1)[-1].isdigit() for url in urls
    ) or len(urls) != len(set(urls)):
        raise ValueError('起票IssueのURL一覧が不正です')
    return report


def run_batch(document, date, weekday, output_dir):
    """stdout/stderrをファイルへ直接保存し、未完了を成功扱いしない。"""
    output_dir.mkdir(parents=True, exist_ok=True)
    summary_file = output_dir / 'summary.md'
    summary_file.write_text('# 夜間調査: 未完了\n\n実行中、または結果を取得できませんでした。\n')
    try:
        tasks = select_tasks(document, weekday)
        (output_dir / 'targets.json').write_text(json.dumps(tasks, ensure_ascii=False, indent=2))
        prompt = f'''KoDriverの夜間調査を行ってください。日付: {date}、JST曜日: {weekday}。
以下のID付き項目だけを調査してください。docs/nightly-todo-list.mdを読み、使い方と毎晩項目の共通ルール（ページ送り・日時判定・いいね数の扱い等）を遵守してください。
{json.dumps(tasks, ensure_ascii=False)}
改善案は対象コードを読んで適用可能性を確認し、gh issue list --state all --searchで重複を確認します。
必要ならgh issue viewで本文を確認します。重複がなければgh issue createで日本語のIssueをラベルなしで起票します。
このバッチはユーザーが承認したIssue起票の自動処理です。起票の追加確認は不要です。
リポジトリのファイルは変更しません。承認が必要になった場合は人間の返答を求めず該当項目をblockedにしてください。
checksには全IDについて確認内容、起票有無と理由（起票した場合は番号）をdetailsへ記入してください。
未調査、アクセス失敗、承認が必要な項目をcompletedにしないでください。
summaryには全体の要約と、Zenn・Qiitaで過去24時間以内に取得した全記事のURL・1行要約を含めてください。
issue_urlsは今回実際に起票したIssueのURLだけとし、起票ゼロなら空配列にしてください。
'''
        with (output_dir / 'response.json').open('w') as stdout, (output_dir / 'stderr.log').open('w') as stderr:
            result = subprocess.run([
                'claude', '-p', prompt, '--allowedTools', ALLOWED_TOOLS,
                '--disallowedTools', 'Edit,Write', '--permission-mode', 'dontAsk',
                '--model', 'sonnet', '--effort', 'low', '--output-format', 'json',
                '--json-schema', json.dumps(result_schema(tasks)),
            ], stdout=stdout, stderr=stderr, check=False)
        (output_dir / 'exit-code.txt').write_text(str(result.returncode))
        payload = json.loads((output_dir / 'response.json').read_text())
        report = validate_result(payload, tasks, result.returncode)
        lines = [f'# 夜間調査: 完了\n\n起票件数: {len(report["issue_urls"])}\n', report['summary']]
        lines += [f'\n## {key}\n{check["details"]}' for key, check in report['checks'].items()]
        lines += ['\n## 起票Issue\n' + ('\n'.join(report['issue_urls']) or 'なし')]
        summary_file.write_text('\n'.join(lines))
        return 0
    except (ValueError, KeyError, IndexError, StopIteration, OSError) as error:
        summary_file.write_text(f'# 夜間調査: 未完了\n\n理由: {error}\n\n'
                                '起票件数: 未確認（途中で起票された可能性があります）。\n'
                                'Artifactのresponse.json、stderr.log、targets.jsonを確認してください。\n')
        return 1


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--date', required=True)
    parser.add_argument('--weekday', required=True, choices=WEEKDAYS)
    parser.add_argument('--output-dir', type=Path, required=True)
    args = parser.parse_args()
    raise SystemExit(run_batch(Path('docs/nightly-todo-list.md').read_text(), args.date,
                               args.weekday, args.output_dir))
