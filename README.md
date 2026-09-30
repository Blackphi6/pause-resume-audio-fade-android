# Pause Resume Audio Fade (Android)

一時停止/再開のたびに音がぶつっと切れたり急に戻ったりするのを、短いフェードでやわらげるAndroidアプリ。

姉妹プロジェクト:
- [Chrome/Firefox拡張機能](https://github.com/Blackphi6/pause-resume-audio-fade)
- [macOSメニューバーアプリ](https://github.com/Blackphi6/pause-resume-audio-fade-mac)

## 仕組みと、Mac/Windows版との違い

Androidにはアプリごとの音量を直接操作するAPIも、macOSのようなプロセス単位の音声タップもありません（root権限なしでは）。そのため対象アプリだけを狙い撃ちするのではなく、**`MediaSessionManager`でシステム全体の再生/一時停止状態を検知し、端末全体のメディア音量（`STREAM_MUSIC`）をなめらかに上げ下げする**方式を採っています。スマホでは同時に鳴っているアプリが1つなことが多いので、体験としては近いものになります。

Android の音量は連続値ではなく離散ステップ（大体15〜25段階）なので、Mac/Windows版のような滑らかなカーブではなく、各ステップへ均等間隔で移動する形のフェードです。

## 必要な権限

「通知へのアクセス」（Notification Listener）。`MediaSessionManager#getActiveSessions()` を呼ぶために必要で、通知の中身自体は読みません。アプリを開いて案内に従ってください。

## 構成

- `VolumeFader` / `fadeSteps`: 音量のフェード計算（純粋関数、ユニットテストあり）
- `AndroidVolumeController`: `AudioManager` のラッパー
- `FadeNotificationListenerService`: システム全体のメディアセッションを監視し、再生状態の変化でフェードを発火
- `MainActivity`: 通知アクセスの許可状態表示と設定画面への導線

## ビルド

```
./gradlew assembleDebug   # APK
./gradlew test            # ユニットテスト
```

JDK 17を推奨（Android Studio同梱のJBRが新しすぎる場合、Kotlinコンパイラがバージョン文字列を解釈できずビルド失敗することがある）。

## 動作確認状況

- ユニットテスト: 通過
- エミュレーター（Medium_Phone API 36.1）へのインストール・起動・通知リスナーサービスのバインドまで確認済み
- 実際のメディアアプリ（YouTube Musicなど）を使ったエンドツーエンドのフェード動作は未検証。実機での確認を推奨

## 既知の制約

- 対象アプリだけでなく端末全体の音量を動かす（Android APIの制約による設計上の割り切り）
- 音量は離散ステップなので、フェードは完全な連続カーブにはならない
- 「通知へのアクセス」という強めの権限が必要（Play Store掲載時は説明が必須になる見込み）
