# Watermark Check-in Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Kotlin Android watermark camera MVP with manual and smart location variants from one codebase.

**Architecture:** A single Gradle project uses Android product flavors to package two apps. Pure business behavior lives in JVM-testable Kotlin modules, while the Android app module wires Compose, CameraX, MediaStore, permissions, and flavor-specific location providers.

**Tech Stack:** Kotlin, Gradle Kotlin DSL, Android Gradle Plugin, Jetpack Compose, CameraX, coroutines, Kotlin serialization, JUnit.

---

## File Structure

- `settings.gradle.kts`: repository and module list.
- `build.gradle.kts`: root plugin versions.
- `gradle/libs.versions.toml`: dependency catalog.
- `app/build.gradle.kts`: Android app, Compose, CameraX, flavors.
- `app/src/main/AndroidManifest.xml`: shared permissions and launcher activity.
- `app/src/manual/AndroidManifest.xml`: removes Internet permission.
- `app/src/smart/AndroidManifest.xml`: keeps Internet permission.
- `app/src/main/java/com/checkin/watermark/MainActivity.kt`: app entry and permission setup.
- `app/src/main/java/com/checkin/watermark/camera/CameraScreen.kt`: CameraX preview and shutter UI.
- `app/src/main/java/com/checkin/watermark/watermark/WatermarkOverlay.kt`: Compose overlay.
- `core/domain/src/main/kotlin/...`: pure domain models and watermark text builder.
- `core/domain/src/test/kotlin/...`: JVM domain tests.
- `core/record/src/main/kotlin/...`: hash and capture record helpers.
- `core/record/src/test/kotlin/...`: hash tests.
- `location/common/src/main/kotlin/...`: location contracts.
- `location/manual/src/main/kotlin/...`: manual provider.
- `location/manual/src/test/kotlin/...`: manual provider tests.
- `location/smart/src/main/kotlin/...`: smart resolver and backend interface.
- `location/smart/src/test/kotlin/...`: smart priority and cache tests.

## Tasks

### Task 1: Scaffold Gradle project

- [ ] Create root Gradle files and module directories.
- [ ] Configure Android app flavor dimensions and product flavors.
- [ ] Add package names, min SDK, target SDK, Compose, CameraX, and test dependencies.
- [ ] Run `./gradlew projects` and fix Gradle configuration errors.

### Task 2: Domain logic with TDD

- [ ] Write failing tests for watermark text generation.
- [ ] Implement `Coordinate`, `LocationSnapshot`, `WatermarkTemplate`, and `WatermarkTextBuilder`.
- [ ] Run domain tests until green.

### Task 3: Record hashing with TDD

- [ ] Write failing tests for stable SHA-256 output.
- [ ] Implement hash utility and capture record model.
- [ ] Run record tests until green.

### Task 4: Manual location with TDD

- [ ] Write failing tests for last-location behavior.
- [ ] Implement manual location repository abstraction and in-memory provider.
- [ ] Run manual location tests until green.

### Task 5: Smart location with TDD

- [ ] Write failing tests for priority: site match, cache, backend, fallback.
- [ ] Implement site matcher, address cache, backend contract, and resolver.
- [ ] Run smart location tests until green.

### Task 6: Android app shell

- [ ] Implement `MainActivity`.
- [ ] Implement Compose camera screen with CameraX preview.
- [ ] Implement live watermark overlay using domain text builder.
- [ ] Wire capture button to CameraX `ImageCapture`.
- [ ] Save captured image placeholder flow and prepare renderer seam.

### Task 7: Flavor wiring

- [ ] Ensure `manual` variant excludes Internet permission.
- [ ] Ensure `smart` variant includes Internet permission.
- [ ] Wire `BuildConfig.SMART_LOCATION` to choose provider path.
- [ ] Run `./gradlew :app:assembleManualDebug :app:assembleSmartDebug`.

### Task 8: Verification

- [ ] Run all JVM tests.
- [ ] Run both debug assembles.
- [ ] Inspect generated manifests for Internet permission difference.
