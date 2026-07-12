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

---

## テスト

- **対象**: `app/desktopApp/src/test/kotlin/kurou/kodriver/AppTest.kt`
  **課題**: `AppUpdateRepository`, `ExitConfirmationEnabledRepository`, `ThemePreferencesRepository`,
  `KeepScreenOnEnabledRepository`, `Gt7Ps5MyBestLapPreferencesRepository`,
  `Gt7Ps5RemainingFuelLapsEnabledRepository`, `Gt7Ps5RemainingFuelLapsPreferencesRepository`,
  `LmuWindowsMyBestLapEnabledRepository`, `LmuWindowsMyBestLapPreferencesRepository`,
  `ReadoutStartSoundPreferencesRepository`, `ConsoleAddressPreferencesRepository` はFakeが用意されておらず、
  本番の `desktopDataModule`（実DataStore、`~/.kodriver` 配下への実ファイル書き込み）がそのまま使われている。
  `AppUpdateRepository` はGitHubへの実ネットワークアクセスも発生しうる。テスト実行のたびに実行環境の
  実際の設定ファイルを書き換えてしまうリスクがある。
  **改善案**: これらもFake Koinモジュールに含め、実DataStore/実ネットワークアクセスをテストから排除する。
