# ビルドとリリース

[English](../../BUILDING.md) · 日本語

通常の利用者は配布APKをインストールしてください。以下は開発・保守向けで、Android Studioは必須ではありません。

## 必要なもの

- JDK 17。
- Android SDK Platform 35、Build Tools 35.0.0。SDKのライセンスに同意済みであること。
- 初回の依存関係取得用のインターネット接続。Gradle 8.7、Android Gradle Plugin 8.6.1、テスト用ライブラリーを取得します。

ANDROID_HOMEにSDKの場所を設定するか、local.propertiesのsdk.dirに自分のSDKのパスを指定します。local.propertiesは公開・コミットしないでください。

リポジトリのルートでWindows PowerShellを開き、実行します。

~~~powershell
.\gradlew.bat assembleRelease lintRelease testReleaseUnitTest
~~~

署名用の環境変数がなければ、app/build/outputs/apk/release/app-release-unsigned.apkが出力されます。未署名APKはインストールできません。ファイル名を変更するだけでは署名されません。

## 署名を非公開で管理する

AndroidではAPKの署名が必要です。同じ鍵を長期保管し、更新時にも使ってください。[Androidの署名ガイド](https://developer.android.com/studio/publish/app-signing?hl=ja)を参照できます。リリースごとに別の鍵を作ったり、自動的にデバッグ署名へ切り替えたりしないでください。

秘密のビルド環境またはシークレット管理機能で、次を設定します。

- CLEANFRAME_SIGNING_KEYSTORE：非公開キーストアへの絶対パス。
- CLEANFRAME_SIGNING_STORE_PASSWORD：キーストアのパスワード。
- CLEANFRAME_SIGNING_KEY_ALIAS：鍵のエイリアス。
- CLEANFRAME_SIGNING_KEY_PASSWORD：鍵のパスワード。

同じビルドコマンドで、署名済みのapp/build/outputs/apk/release/app-release.apkを生成できます。リリースビルドのデバッグは無効です。鍵、パスワード、環境ファイルを、リポジトリ、公開ファイル、ログ、ソースZIPに含めないでください。既存配布版は更新互換性のために元の証明書を維持しています。証明書名がAndroid Debugでも、アプリのデバッグが有効という意味ではありません。別の鍵では上書き更新できません。

## 確認して配布する

1. ビルド、lint、API 29のRobolectricテストを実行します。テスト用の依存関係はAPKに入りません。
2. apksignerで署名を検証し、aaptやapkanalyzerでアプリID、バージョン、最低SDK、権限、debuggableを確認します。
3. 実機でインストール・更新とホームボタンを確認します。自動テストだけではメーカーのファームウェアを保証できません。
4. 確認したソースと署名済みAPKだけを配布します。LICENSEと素材の表記を残し、build、キャッシュ、local.properties、鍵、環境ファイル、引き継ぎ資料、診断ファイルを除外します。.gitignoreだけでなく、実際の内容も調べます。
5. 最終ファイルのSHA-256を計算し、配布時に添付します。秘密の署名フォルダーは公開しません。

## 言語対応

アプリ自身の文字列はvalues/strings.xmlとvalues-ja/strings.xml、対応付けはUiText.javaで管理します。新しい画面やダイアログは両言語を用意してください。インストール済みアプリの表示名を翻訳カタログに通さないでください。変更後は、言語の保存、ダイアログの確認・キャンセル、リモコンのフォーカスをテストします。

このプロジェクトのビルド処理が、自動でテレビにインストールしたり設定を変更したりすることはありません。ソースの公開だけでAPKリリースが自動公開されるわけではありません。
