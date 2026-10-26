# Verification — 1.1.0

English · [日本語](docs/ja/VERIFICATION.md)

Built with Gradle 8.7, Android Gradle Plugin 8.6.1, JDK 17 and SDK/Build Tools 35, without Android Studio.

## Automated checks

- Release compilation, lint and 43 API 29 Robolectric tests passed: 26 UI tests and 17 Home-service tests, with no failures or skips.
- English/Japanese screen text and resource parity checked. Tests cover language persistence, keeping installed app labels unchanged, Japanese dialogs/statuses, Google-launcher OK/Cancel behavior and remote focus.
- The warning does not alter Home interception or launch another app until OK; Cancel returns to the same menu. OK pauses interception before requesting the stock launcher.
- Native 1920×1080 renders reviewed in both languages. Pixel checks verify a linear fade beginning near the n in Settings, no clock shade, no shading beyond the intended edge and retained footer shading. Dithering is enabled and the local tint is softer than 1.0.2; actual TV banding cannot be certified by these renders.
- Existing navigation, wallpaper decoding, protected-package guidance, preference preservation and Home input handling tests remain in place.
- Lint has no errors. Two warnings remain: fixed landscape orientation and the square launcher icon.
- APK signature, version 1.1.0 / code 11, org.cleanframe.sharp, min/target API 29, absence of network permissions/debuggable/testOnly flags, and disabled backup are inspected before packaging.
- The signing certificate matches previous distributed builds for in-place updates. Its generic Android Debug identity is not a personal publisher identity and does not make the release application debuggable.
- The Home filter and controller behavior are unchanged. Only UI/resources, confirmation flow and the app’s saved language preference changed; no automatic package or system-setting changes were added.

## Physical-test limits

App versions through 1.0.2 were reported working on Sharp AQUOS 4T-C45BL1 / AQUOS-TVJ19, Android TV 10. Version 1.1.0 still needs physical installation/update, Japanese remote navigation, dialog, fade smoothness, first-press Home, wake/boot and firmware menu checks. The exact firmware build identifier is not recorded. Other devices/firmware remain unverified. The Google shortcut targets com.google.android.tvlauncher, not every Google TV launcher family.

The corrected Accessibility path is the user-reported 設定 → その他の設定 → ユーザー補助, with Services at the bottom. The effect of disabling debugging on that firmware’s menu visibility has not been tested; the guide does not claim the menu disappears. Keeping the Accessibility service and Home interception enabled is separate from disabling debugging.

## Public distribution

The reviewed source excludes private handovers, diagnostics, local SDK paths, build logs/caches, Git history, keys and passwords. Public model/OS compatibility was explicitly requested for documentation; actual device addresses and identifiers remain excluded. Example addresses are fictional. Photographs/fonts are credited public assets; screenshots use synthetic app fixtures. Source and APK archive entries are scanned for private identifiers and local paths. Source/APK assets are checked for matching contents. Original legal notices are retained with Japanese explanations.

APK SHA-256: `8ac0492bc2cb1da1de0a793b93db93aa40fc65126ae23e1bbe5dc9914780803c`.
