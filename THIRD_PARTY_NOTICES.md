# Third-party notices

## Shizuku API

This project uses the official Shizuku API SDK, version 13.1.5:

- `dev.rikka.shizuku:api:13.1.5`
- `dev.rikka.shizuku:provider:13.1.5`
- `dev.rikka.shizuku:aidl:13.1.5`
- `dev.rikka.shizuku:shared:13.1.5`

Project: https://github.com/RikkaApps/Shizuku-API

The SDK is licensed under the MIT License. Its upstream copyright and license text are preserved in `licenses/Shizuku-API-LICENSE.txt`. Dependency binaries are fetched through Maven and not committed here.

## Build tools

Android SDK, Android Gradle Plugin, R8/D8 and apksig are external build dependencies with their respective licenses. They are not vendored in this repository. Java standard libraries and Android framework APIs are supplied by the user's build/runtime environment.

## vivo-specific interoperability

The application refers to vivo package names, settings keys and exported activities for on-device interoperability. vivo system APKs, extracted assets and decompiled source are **not included** in the published project. The illustrative foldable phone drawing is local Canvas code, not an extracted vivo asset.

Installed application icons are loaded on the user's device by Android PackageManager. Such icons are not redistributed as project resources.

No CoverScreen OS source, APK, paid-license bypass or proprietary resources are included.
