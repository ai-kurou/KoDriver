# Android 版のインストール手順

## インストール

1. [Releases](https://github.com/ai-kurou/KoDriver/releases) から最新の APK（`KoDriver-android-*.apk`）をダウンロードします。
2. ダウンロードした APK ファイルを開いて実行します。
3. 提供元不明のアプリのインストールを許可するよう求められた場合は、画面の指示に従ってこのインストールを許可してください。

インストール後、アプリ一覧からKoDriverを起動してください。

読み上げ音声の準備と、Android 17（API 37）以降のローカルネットワーク許可については、[README.md の「Android アプリ」節](../README.md) を参照してください。

## マイクの許可（音声認識）

Android では、音声認識のために実行時にマイク（`RECORD_AUDIO`）の許可を求めます。

- 許可しない・拒否した場合は音声認識を使えず、右下のチップがエラー表示になります。その場合は端末の設定でKoDriverのアプリ情報を開き、マイクの権限を許可してください。
- 起動時にマイクの権限が許可済みの場合だけ、音声認識が自動で ON になります。未許可のときは権限ダイアログを出しません。右下のチップをタップして開始すると、そのときに権限ダイアログが表示されます。

## 設定のバックアップと復元

KoDriver の設定は、Google のバックアップ機能（端末の自動バックアップ）の対象です。端末を買い替えた場合も、Google アカウントのバックアップから設定が復元されるため、KoDriver で操作する必要はありません。

- 設定が復元されない場合は、端末の Google アカウントのバックアップが有効になっているか、新しい端末を同じ Google アカウントで設定したかを確認してください。
- 音声認識用のデータとテレメトリログは、バックアップの対象外です。

## Android の開発者認証（Developer verification）について

Google は、認定された Android 端末で、Android 本体に登録されていない開発者のアプリのインストールを制限する「Android developer verification」を導入します。2027年1月からグローバル展開が始まり、認定された Android 端末（Android 8 / API 26 以上）のすべてのインストール元（サイドロードを含む）に、数か月かけて対象が広がる予定です。

開発者として登録が完了していないアプリは、「Advanced flow」を設定しない限りインストールや更新ができなくなります。KoDriver の APK は GitHub Releases から直接配布しているため、開発者登録が完了していない場合は、この設定が必要になることがあります。この設定が不要になるのは、開発者の本人確認に加えて、KoDriver のパッケージ名と署名鍵の登録が完了した場合です。

### Advanced flow の設定手順の概要

Advanced flow の設定は端末ごとに行い、有効期間（「7日間」または「無期限」）の間は再設定が不要です。詳細な手順は、画面表記が端末メーカーや Android のバージョンによって異なるため、公式ヘルプで確認してください。

1. 端末の Developer Mode（開発者向けオプション）を有効にします。
2. 第三者から指示されて操作していないかを確認します。
3. 端末を再起動し、再認証を行います。
4. 24時間待機した後に設定画面へ戻り、生体認証または PIN で本人確認を行います。
5. 有効期間として「7日間」または「無期限」のいずれかを選択します。

設定後は、インストールのたびに「未確認の開発者」の警告が表示されることがあります。その場合は「Install Anyway」を選択して進めてください。Developer Mode は設定後に OFF に戻しても構いません。

- 公式ヘルプ: [Android developer verification（ユーザー向けヘルプ）](https://support.google.com/android/answer/17588095)
- 解説ブログ: [Android developer verification（Android Developers Blog）](https://android-developers.googleblog.com/2026/03/android-developer-verification.html)
- FAQ: [Developer verification FAQ](https://developer.android.com/developer-verification/guides/faq)

### KoDriver 利用時の補足

- Advanced flow を無効にすると、未登録のアプリは更新に失敗します。有効期間が「7日間」の場合は期限が切れると無効に戻るため、更新するときは公式ヘルプに従って Advanced flow を再度設定してください。
- 「無期限」を選ぶと更新のたびに設定し直す必要はありませんが、KoDriver 以外の未確認の開発者のアプリも、警告を確認すればインストールできる状態が続きます。更新の手間とのトレードオフを理解したうえで選んでください。
- 未確認の開発者のアプリは、誰かに勧められた APK など、提供元が不明なものをインストールしないでください。KoDriver の APK は、このリポジトリの [Releases](https://github.com/ai-kurou/KoDriver/releases) から入手したものだけを使用してください。
- 「24時間待機」は初回設定時に一度だけ必要です。
- ADB 経由でインストールする場合は、この制限の影響を受けません。
