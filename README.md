# StreamX Skins

Skin extensions for [StreamX](https://github.com/StreamXProject/StreamX).
Install skins from **Settings → Sources → Skins**.

[Downloads](https://github.com/StreamXProject/Skins/releases) · [Repository index](https://github.com/StreamXProject/Skins/releases/download/repo/index.json)

## Building

Requires JDK 21 and the Android SDK.

```sh
./gradlew assembleRelease
```

Builds and publishing are manual. Push your changes, then open **Actions → Build and publish extensions → Run workflow** and select `main`.
Add these repository secrets under **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 of the existing release keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Existing signing alias |
| `ANDROID_KEY_PASSWORD` | Private-key password |

Use the same release key to preserve extension updates.

Or trigger a release from the terminal:

```sh
gh workflow run build.yml --repo StreamXProject/Skins --ref main
```

Releases use UTC date versions (`vYYYY.MM.DD.HHMMSS`). The `repo` release provides the stable extension index.

### Extension icons

Catalogue entries may include an optional HTTPS `iconUrl`. The build exports each extension’s own Android vector icon as a content-addressed SVG and uploads it beside the APK. The public mirror preserves the icon and rewrites its URL to the public release. StreamX uses installed APK icons first, otherwise loads `iconUrl` through its disk/memory image cache. Missing icons use a generic placeholder; no branded preview icons are bundled in StreamX. Older catalogues without `iconUrl` remain supported.

## Credits

Some skins recreate the look of other open music players. Thanks to:

- [Looper](https://github.com/SthrNilshaaa/looper)
- [Namida](https://github.com/namidaco/namida)
- [DMT](https://github.com/imjyotiraditya/dmt)
