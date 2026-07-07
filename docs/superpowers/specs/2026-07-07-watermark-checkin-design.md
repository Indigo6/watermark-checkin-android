# Watermark Check-in Android Design

## Goal

Build an Android watermark camera app for nationwide field workers. The app opens directly to a camera preview, shows the watermark on top of the preview, and saves a watermarked photo with one tap.

## Product Variants

The project ships two variants from one codebase.

### Manual Location Variant

- Flavor name: `manual`
- App name: `水印打卡-手动版`
- No Internet permission.
- No map SDK or online reverse geocoding.
- Location text comes from the last saved manual value.
- The worker can tap the watermark to edit the location in a large, simple dialog.
- GPS coordinates and accuracy are still captured when permission is granted.

### Smart Location Variant

- Flavor name: `smart`
- App name: `水印打卡-智能版`
- Includes Internet permission.
- Location text is resolved by priority:
  1. Local configured check-in site match.
  2. Cached online address near the current coordinate.
  3. Online reverse geocoding through the owner's backend proxy.
  4. Coordinate fallback.
- The map provider key is not stored in the Android app.

## User Experience

The default screen is a full-screen camera view with a watermark overlay. The primary action is the shutter button. After capture, the app saves a watermarked image and shows a lightweight share action.

The normal worker flow has no forms and no technical language. Advanced settings are hidden behind a settings entry and are not required for daily use.

## Architecture

Use Kotlin, Android Gradle Plugin, Jetpack Compose, CameraX, Room, Kotlin serialization, and coroutines.

Modules:

- `app`: Android entry point, Compose UI, flavor configuration.
- `core:domain`: pure Kotlin models and business logic.
- `core:watermark`: watermark layout and image rendering.
- `core:record`: local capture records and hash calculation.
- `location:common`: common location provider contracts and models.
- `location:manual`: manual location source.
- `location:smart`: local site matching, cache policy, and backend reverse geocoding client.

## Data Flow

1. Camera screen starts and requests camera/location permissions.
2. Location provider emits current coordinate, accuracy, and resolved display text.
3. Watermark overlay renders current time, display location, coordinate, accuracy, and optional worker note.
4. Capture action receives the camera image.
5. Watermark renderer writes the overlay onto the image bitmap.
6. Record repository stores the capture record, source metadata, original hash, and output hash.
7. MediaStore saves the output image.

## Error Handling

- Camera permission denied: show a single clear permission screen.
- Location permission denied: allow photo capture, show configured/manual location and omit GPS details.
- Location unavailable: show `定位中` before first fix and coordinate fallback after timeout.
- Smart reverse geocoding failure: use cache or coordinate fallback.
- Save failure: keep the captured image in app cache and show a retry action.

## Testing

Pure business logic is covered with JVM unit tests:

- Watermark text generation.
- Capture record hash behavior.
- Manual location persistence contract.
- Smart location resolution priority.
- Nearby cache matching.

Android UI and CameraX integration are left for instrumented/manual verification because camera hardware and permissions are device-dependent.

## Implementation Scope For First Pass

The first implementation pass should produce a compilable Android project with:

- Two flavors: `manual` and `smart`.
- Shared Compose camera shell.
- CameraX preview and photo capture wiring.
- Watermark overlay preview.
- Pure Kotlin domain tests.
- Manual location provider.
- Smart location resolver abstraction with backend-client placeholder and tested priority logic.
- Local record model and hash utility.

The first pass does not need production signing, app-store metadata, real backend deployment, or full administrator management UI.
