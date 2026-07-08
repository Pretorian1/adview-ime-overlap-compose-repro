# AdView + IME overlap issue in Jetpack Compose

This repository contains a minimal reproducible example for an issue where an `OutlinedTextField` placed near the bottom of the screen is overlapped by the software keyboard after the app is minimized and restored.

The issue appears when a Google Mobile Ads `AdView` is loaded inside Jetpack Compose using `AndroidView`.

The same behavior is reproduced with both the legacy Google Mobile Ads SDK and the GMA Next-Gen SDK.

## Environment

Tested with:

* Google Mobile Ads SDK Legacy: `25.4.0`
* GMA Next-Gen SDK: `1.2.1`
* Jetpack Compose BOM: `2026.06.01`
* `compileSdk = 37`
* `targetSdk = 36`
* Test AdMob App ID: `ca-app-pub-3940256099942544~3347511713`
* Test Banner Ad Unit ID: `ca-app-pub-3940256099942544/9214589741`

Legacy Google Mobile Ads SDK dependency:

```toml
adMobVersion = "25.4.0"

play-services-ads = {
    group = "com.google.android.gms",
    name = "play-services-ads",
    version.ref = "adMobVersion"
}
```

GMA Next-Gen SDK dependency:

```toml
adsMobileSdk = "1.2.1"

ads-mobile-sdk = {
    group = "com.google.android.libraries.ads.mobile.sdk",
    name = "ads-mobile-sdk",
    version.ref = "adsMobileSdk"
}
```

## GMA Next-Gen SDK test

The same minimal sample was also migrated to **GMA Next-Gen SDK**.

A separate branch is available here:

```text
gmanext-gen-sdk
```

Result: the issue is still reproducible with GMA Next-Gen SDK.

This means the issue does not appear to be limited to the legacy Google Mobile Ads SDK dependency:

```kotlin
com.google.android.gms:play-services-ads
```

The same behavior is reproduced when using the newer GMA Next-Gen SDK `AdView` embedded in Jetpack Compose via `AndroidView`.

The reproduction steps are the same as in the main branch.

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

* Google Mobile Ads `AdView` in both legacy and Next-Gen SDKs
* `AdView.loadAd()`
* Jetpack Compose `AndroidView`
* Android software keyboard / IME lifecycle
* Activity background / foreground restore flow

The issue does not appear to be caused by:

* `OutlinedTextField`
* `Scaffold`
* `AndroidView` itself
* `AdView` without calling `loadAd()`

The strongest evidence is that commenting out only the ad loading call makes the issue disappear.

Legacy Google Mobile Ads SDK example:

```kotlin
adView.loadAd(
    AdRequest.Builder().build()
)
```

GMA Next-Gen SDK example:

```kotlin
adView.loadAd(
    adRequest,
    object : AdLoadCallback<BannerAd> {
        override fun onAdLoaded(ad: BannerAd) {
            // Ad loaded
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            // Ad failed to load
        }
    }
)
```