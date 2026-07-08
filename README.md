# Watermark Check-in Android

Android watermark camera app for field check-ins.

## Variants

This repository builds two apps from one codebase:

- `manual`: manual location text, no Internet permission, no map SDK.
- `smart`: smart location path, with Internet permission and a backend reverse-geocoding extension point.

## Build Commands

```bash
./gradlew :app:assembleManualDebug
./gradlew :app:assembleSmartDebug
```

Release variants:

```bash
./gradlew :app:assembleManualRelease
./gradlew :app:assembleSmartRelease
```

## Environment

Required:

- JDK 17
- Android SDK
- Android Gradle Plugin compatible Gradle

The initial server can run Gradle after installing user-local JDK 17 and Android SDK, but it has limited CPU and memory. Use GitHub Actions or a stronger local machine for APK builds.

GitHub Actions is configured in `.github/workflows/android-ci.yml` to run tests, build both debug APKs, and upload them as workflow artifacts on every push to `main`.

## Current MVP Behavior

- Opens directly to a CameraX preview after camera permission is granted.
- Shows a live watermark overlay.
- Manual flavor lets the worker tap the watermark and edit the location text.
- Capture writes the camera image to cache, renders the watermark into the bitmap, and saves the result to the gallery.
- Shared domain logic covers watermark text generation, coordinates, source metadata, and location resolution.
- Smart flavor currently contains the local matching/cache/backend abstraction. A real backend endpoint still needs to be wired.

## Smart Location Strategy

The smart flavor should resolve display location in this order:

1. Configured local check-in site near the GPS coordinate.
2. Cached address near the GPS coordinate.
3. Backend reverse-geocoding proxy.
4. Coordinate fallback.

Do not put map provider keys directly in the Android app.
