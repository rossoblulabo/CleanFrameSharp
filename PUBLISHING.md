# Upload to a new GitHub repository

English · [日本語](docs/ja/PUBLISHING.md)

1. Keep the repository private while reviewing the files, or create an empty public repository when ready. Use **CleanFrameSharp** or another project name; this is an unofficial Android adaptation, not the original project.
2. Upload the contents of this source folder as the repository root, including the app and gradle folders, Gradle wrapper, .gitignore, LICENSE and Markdown documents. Do not upload the ZIP itself as a substitute for browsable source.
3. Upload only this reviewed folder. Do not upload its parent workspace, earlier handovers, diagnostics, keys, build folders or other release archives. No Git history is supplied.
4. Once physical testing is satisfactory, create a release tagged **v1.1.0**. Attach **CleanFrameSharp-1.1.0.apk**, the public-source ZIP and SHA256SUMS. GitHub can also generate a source archive from the tag.
5. Use CHANGELOG.md for release notes and link INSTALL-AND-RECOVERY.md. State that other TV models and firmware remain unverified. Keep the unofficial adaptation credit and all licences visible.
6. Future APK updates must use the same private signing certificate. The public source archive deliberately contains no key or password. Keep your private signing backup outside this repository and outside release assets.

Nothing has been uploaded by preparing this folder. Inspect your own GitHub commit author name/email settings before committing if you wish to keep that account metadata private; source sanitization cannot hide information your account publishes.

7. Include both English and Japanese documents and check their language links. Keep the AI-development disclosure and third-party credits visible.
