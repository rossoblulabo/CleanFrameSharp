# Build and release

English · [日本語](docs/ja/BUILDING.md)

End users should install the prebuilt APK; these steps are for maintainers. Android Studio is optional.

## Requirements

- JDK 17.
- Android SDK platform 35, Build Tools 35.0.0 and the SDK licences accepted.
- Internet for Gradle 8.7, Android Gradle Plugin 8.6.1 and test dependencies on the first build.

Point ANDROID_HOME to your SDK installation, or create an untracked local.properties containing sdk.dir with your own SDK path. Never commit local.properties.

From the repository root in Windows PowerShell:

~~~powershell
.\gradlew.bat assembleRelease lintRelease testReleaseUnitTest
~~~

Without signing environment variables, assembleRelease produces **app/build/outputs/apk/release/app-release-unsigned.apk**. It cannot be installed until signed. Do not rename an unsigned APK and call it installable.

## Private signing

Android requires signed APKs. Use a private long-lived signing key and retain it for updates. See [Android signing documentation](https://developer.android.com/studio/publish/app-signing). Do not create a fresh key for every release or automatically fall back to debug signing.

Set these environment variables in your own private build session or secret store:

- CLEANFRAME_SIGNING_KEYSTORE: absolute path to the private keystore.
- CLEANFRAME_SIGNING_STORE_PASSWORD: keystore password.
- CLEANFRAME_SIGNING_KEY_ALIAS: signing alias.
- CLEANFRAME_SIGNING_KEY_PASSWORD: key password.

Then run the same build command. The signed output is **app/build/outputs/apk/release/app-release.apk**. Release builds have debugging disabled. Keep keystores, passwords and local environment files out of the repository, release assets, logs and source ZIPs. Existing distributed builds retain their original signing certificate for update compatibility; its generic Android Debug certificate name does not mean the release application is debuggable. A build from a different key cannot update it in place.

## Check and package

1. Run the build, lint and API 29 Robolectric tests. Test-only dependencies are not bundled into the APK.
2. Verify the APK with the SDK apksigner, and inspect package ID, version, minimum SDK, manifest permissions and debuggable status with aapt/apkanalyzer.
3. Check install/update and physical Home behavior on a real TV. Automated checks cannot certify manufacturer firmware.
4. Publish only reviewed source and explicitly chosen signed APK assets. Preserve the LICENCE/notice files. Exclude all build folders, caches, local.properties, keys, environment files, handovers and device diagnostics. Scan filenames and file contents, not only .gitignore.
5. Calculate SHA-256 for the final files and include the checksums with the release. Never upload the private signing folder.

No workflow automatically installs on a TV or changes TV settings. Uploading source does not publish an APK release automatically.

## Languages

App-owned strings use values/strings.xml and values-ja/strings.xml, with UiText.java mapping the source text. Update both languages when changing UI. Never translate installed app labels through this catalogue. Check language persistence, dialog confirmation/cancellation and D-pad focus.
