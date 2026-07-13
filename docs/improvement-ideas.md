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

## core:lmu-windows-data

- **対象**: `core/lmu-windows-data/.../mapper/LmuWindowsMapper.kt`、`core/domain/.../model/LmuWindowsTimingData.kt`
  **課題**: `LmuWindowsTimingData.sector2Ms` には共有メモリの `mBestLapSector2` を格納しているが、このフィールドは S1+S2 の累積値であり、セクター2単体のタイムではない。フィールド名からは S2 単体に見えるため、将来これを表示・読み上げに使うと誤ったタイムを扱うバグになる（現時点で `sector1Ms` / `sector2Ms` を消費する実装はなく実害は未発生）。また `mBestLapSector1/2` は「ベストセクタータイム」ではなく「ベストラップ中のセクタータイム」である点も要注意。
  **改善案**: 消費側を実装するときに `sector2Ms - sector1Ms` で S2 単体を算出するか、フィールド名を `sector1And2Ms` などの累積値であることが分かる名前に変更する。詳細は `docs/lmu-windows-telemetry.md` の注意事項を参照。

- **対象**: `core/lmu-windows-data/.../mapper/LmuWindowsMapper.kt`
  **課題**: `MAX_SCORING_VEHICLES = 128` は rF2 プラグイン由来の値で、LMU の `vehScoringInfo` 配列は 104 要素（`MAX_MAPPED_VEHICLES`）。`mNumVehicles` が万一 104 を超える値だった場合、105 台目以降の探索は配列末尾を越えて `scoringStream` 領域を車両データとして読むことになる（実際に LMU が 104 超を返す可能性は低く、実害はほぼない）。
  **改善案**: `MAX_SCORING_VEHICLES` を 104 に修正する。
