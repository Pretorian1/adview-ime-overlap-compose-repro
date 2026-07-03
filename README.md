# AdView + IME overlap issue in Jetpack Compose

This repository contains a minimal reproducible example for an issue where an `OutlinedTextField` placed near the bottom of the screen is overlapped by the software keyboard after the app is minimized and restored.

The issue appears only when a Google Mobile Ads `AdView` is loaded via `loadAd()` inside Jetpack Compose using `AndroidView`.

## Environment

Tested with:

* Google Mobile Ads SDK: `25.4.0`
* Jetpack Compose BOM: `2026.06.01`
* `compileSdk = 37`
* `targetSdk = 36`
* Test AdMob App ID: `ca-app-pub-3940256099942544~3347511713`
* Test Banner Ad Unit ID: `ca-app-pub-3940256099942544/9214589741`

## Devices / Android versions

Observed behavior:

| Android version | Result                                      |
| --------------- | ------------------------------------------- |
| Android 8.1     | Not reproduced without `enableEdgeToEdge()` |
| Android 10      | Reproduced                                  |
| Android 16      | Reproduced                                  |

Additional observation:

* With `enableEdgeToEdge()`, the issue can also be reproduced on Android 8.1.
* The main reproduction case does **not** require `enableEdgeToEdge()`.

## Reproduction steps

1. Launch the app.
2. Tap the `OutlinedTextField` at the bottom of the screen.
3. Wait until the software keyboard appears.
4. Press the **Home** button.
5. Return to the app.
6. The software keyboard overlaps the bottom `OutlinedTextField`.

## Expected behavior

After returning to the app, the `OutlinedTextField` should remain visible above the software keyboard.

## Actual behavior

After returning to the app, the software keyboard overlaps the `OutlinedTextField`.

## Important observation

If the issue is reproduced and the device orientation is changed, the layout becomes correct again.

After rotation, the keyboard works normally until the app is minimized and restored again.

This suggests that a full Activity recreation / layout remeasurement fixes the state, while returning from background with an already loaded `AdView` can leave the IME/layout state incorrect.

## Control experiments

### 1. `AdView` with `loadAd()`

```kotlin
TopBannerAd()
```

Result: issue is reproduced.

### 2. `AdView` without `loadAd()`

```kotlin
TopBannerAd(loadAd = false)
```

Result: issue disappears.

### 3. Replace `AdView` with `TextView` inside `AndroidView`

```kotlin
AndroidView(
    factory = {
        TextView(it).apply {
            text = "Banner"
        }
    }
)
```

Result: issue disappears.

## Investigation summary

The issue appears to be caused by the interaction between:

* Google Mobile Ads SDK `AdView`
* `AdView.loadAd()`
* Jetpack Compose `AndroidView`
* Android software keyboard / IME lifecycle
* Activity background / foreground restore flow

The issue does not appear to be caused by:

* `OutlinedTextField`
* `Scaffold`
* `AndroidView` itself
* `AdView` without calling `loadAd()`

The strongest evidence is that commenting out only this line makes the issue disappear:

```kotlin
adView.loadAd(
    AdRequest.Builder().build()
)
```