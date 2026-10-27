# CleanFrame Sharp

[English](README.md) · 日本語

**Android TV搭載のシャープ製AQUOS向けに開発したランチャー**です。お気に入りのアプリ、写真、時刻だけをすっきりと表示します。[Nathan Clark氏のCleanFrame](https://github.com/nthnclrk/CleanFrame)を参考に制作した、Android向けの非公式アプリです。

**開発の対象機種はSharp AQUOS 4T-C45BL1（AQUOS-TVJ19）、Android TV 10／API 29です。他機種や別のファームウェアでの動作は未確認です。**

**このAndroid実装のコードは、元のCleanFrameのGitHubリポジトリを参考に、ChatGPT Codexを使って100% AIで作成しました。** 要件の指定、判断、テレビ実機での確認は人が行っています。元のデザイン、写真、フォントの制作者には、それぞれクレジットを記載しています。

Javaで実装した独立したAndroidアプリです。元のTizen用HTML/CSS/JavaScriptや、参考にした[Depth118氏のAndroid移植版](https://github.com/Depth118/cleanframe-androidtv)のWebView実装は実行していません。CleanFrame、シャープ、Googleの公式製品ではありません。

## 主な機能

- リスト／ドック表示、リモコン操作、ホームのアプリの並べ替え。
- クレジット付きの壁紙15枚、グラデーション、自分で選んだ写真。
- フォント、文字サイズ・太さ、背景の暗さ、選択表示の調整。
- 日本語／英語表示。インストール済みアプリ名はAndroidが提供する表示名を維持。
- 任意のホームボタン割り当て。元のランチャーも利用可能。
- システムパッケージの状態表示のみ。広告、利用状況の解析、ネットワーク権限なし。

## 画面例

![リスト表示](docs/images/list.png)

![日本語の設定画面](docs/images/settings-ja.png)

テスト用の仮アプリで自動描画した画像です。実際のアプリアイコンはテレビから取得します。

## インストール

ReleasesからAPKをダウンロードし、[インストールと復旧の手順](docs/ja/INSTALL-AND-RECOVERY.md)に従ってください。Android Studioは不要です。

動作報告のある環境：**Sharp AQUOS 4T-C45BL1（AQUOS-TVJ19）、Android TV 10／API 29**。アプリ1.0.2まで確認報告があります。日本語表示などを追加した1.1.0のテレビ実機での確認は、まだ完了していません。ファームウェアの正確なビルド番号は記録されていません。他機種・別のファームウェアは未確認です。Android 10以上が必要です。

## システムサービスの任意設定

[任意のサービス無効化ガイド](docs/ja/OPTIONAL-SERVICES.md)に、無効化の報告があった3つのパッケージ、考えられる影響、有効・無効を切り替えるコマンドを掲載しています。**CleanFrameの利用に必要な操作ではありません。**

## プライバシーとクレジット

[プライバシー](docs/ja/PRIVACY.md) · [第三者素材の出典とライセンス](docs/ja/THIRD_PARTY_NOTICES.md) · [MITライセンス原文](LICENSE) · [ライセンスの日本語説明](LICENSE.ja.md)。

元のCleanFrameはNathan Clark氏、2026年著作、MITライセンスです。このAndroidプロジェクトのコードと文書もMITライセンスです。写真とフォントは各素材のライセンスに従います。提携・公認を示すものではありません。

## 開発・公開

[ビルド](docs/ja/BUILDING.md) · [貢献](docs/ja/CONTRIBUTING.md) · [変更履歴](docs/ja/CHANGELOG.md) · [検証](docs/ja/VERIFICATION.md) · [GitHubへの公開](docs/ja/PUBLISHING.md)。

各文書の先頭から日本語／英語を切り替えられます。第三者のライセンス原文は変更せず、日本語の説明を別途用意しています。不具合報告には、IPアドレス、アカウント情報、機器の識別情報を含めないでください。
