# Google Play ローカルネットワーク権限の申告メモ

`ACCESS_LOCAL_NETWORK` は Android 17（API 37）以降をターゲットとするアプリで Google Play のポリシー対象になる（発効予定: 2027-08-25。[ポリシー](https://support.google.com/googleplay/android-developer/answer/16909972?hl=ja)）。KoDriver は `targetSdk = 37` のため、Play Console での申告が必要になる。

## アプリ側の対応状況

- `app/androidApp/src/main/AndroidManifest.xml` に `ACCESS_LOCAL_NETWORK`（`minSdkVersion="37"`）を宣言済み。
- `MainActivity` が起動時に、API 37 以上かつ未許可の場合のみランタイム許可を要求する。
- 「その他 → ローカルネットワークへのアクセス許可」で許可状態の確認とアプリ設定への遷移ができる。

## 申告文の下書き

### ローカルネットワークアクセスが必要な理由

KoDriver の Android 版は、同じ LAN 内の次の機器と通信してレーシングシミュレーターの走行情報を受け取り、音声で読み上げる。

- Windows 上の KoDriver デスクトップアプリの WebSocket サーバー（LMU / ACE の走行情報）
- Gran Turismo 7 を動かす PS5、または SimHub からの UDP テレメトリ

この通信が許可されないとアプリのコア機能（走行情報の取得と読み上げ）が動作しない。

### デバイス選択ツールでは不十分な理由

接続先は、ユーザーが LAN 内で任意に用意した PC / PS5 であり、アプリが事前に対象の種類や数を限定できない。PS5 や SimHub は mDNS 等で広告されないため `NsdManager` では検出できず、ユーザーが IP アドレスを入力して UDP / WebSocket で直接接続する必要がある。KoDriver サーバーは mDNS（`_kodriver._tcp.local.`）で検出して IP を自動入力できるが、PS5 / SimHub への接続と任意の IP 指定ではデバイス選択ツールの範囲を超える。

## 提出時の確認事項

- 提出期限（2027-08-25）前に Play Console で申告する。
- 通信先・用途が上記から変わった場合は、この文面を更新する。
