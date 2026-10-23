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
