# Credits and third-party licences

English · [日本語](docs/ja/THIRD_PARTY_NOTICES.md)

## CleanFrame

Visual design and interaction inspiration: [CleanFrame](https://github.com/nthnclrk/CleanFrame), copyright (c) 2026 Nathan Clark, MIT. The original MIT notice is retained at [CleanFrame-MIT.txt](app/src/main/assets/licenses/CleanFrame-MIT.txt). This separate native Android implementation reuses the credited public wallpapers and fonts. No official affiliation or endorsement is implied.

[Depth118/cleanframe-androidtv](https://github.com/Depth118/cleanframe-androidtv) was an Android platform reference. Its WebView runtime is not included in this application.

## Fonts

Manrope, Geist and Newsreader use the SIL Open Font License 1.1. Retain the copyright and licence text with the binaries:

- [Manrope licence](app/src/main/assets/licenses/Manrope-OFL.txt); [font source](https://github.com/google/fonts/tree/main/ofl/manrope).
- [Geist licence](app/src/main/assets/licenses/Geist-OFL.txt); [font source](https://github.com/google/fonts/tree/main/ofl/geist).
- [Newsreader licence](app/src/main/assets/licenses/Newsreader-OFL.txt); [font source](https://github.com/google/fonts/tree/main/ofl/newsreader).

No proprietary Pangram font binaries are included. Font licences do not change when embedded in an MIT-licensed app.

## Wallpapers

The fifteen wallpaper photographs and their thumbnails were obtained from CleanFrame’s public assets. Photographers retain their rights. See the [individual source and photographer register](app/src/main/assets/licenses/PHOTO-SOURCES.md), [runtime catalogue](app/src/main/assets/wallpapers/SOURCES.json) and [included Unsplash licence](app/src/main/assets/licenses/Unsplash-License.txt). The upstream source register records its review date; that record is not a claim that this project performed that earlier review.

The [Unsplash License](https://unsplash.com/license) permits use and distribution subject to its restrictions. Do not sell unmodified images or compile them into a competing image service. The project’s MIT licence does not relicense photographs.

## App icons and development tools

Installed-app icons and service names are loaded from the device; third-party streaming-service icon packs are not bundled. Owners retain their trademark rights.

The Gradle wrapper is a build tool distributed under Apache License 2.0; see [its licence](gradle/wrapper/LICENSE-Gradle.txt) and [Gradle’s included notices](gradle/wrapper/NOTICE-Gradle.txt). Android build tools and test dependencies are downloaded separately under their own terms. JUnit and Robolectric are test-only and are not included in the APK. The APK has no third-party runtime library dependency.
