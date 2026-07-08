package com.example.topbannerandbottomtextfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.topbannerandbottomtextfield.ui.theme.TopBannerAndBottomTextFieldTheme

import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(
                this@MainActivity,
                InitializationConfig.Builder("ca-app-pub-3940256099942544~3347511713").build()
            ) {}
        }
        //enableEdgeToEdge()
        setContent {
            TopBannerAndBottomTextFieldTheme {
                var text by rememberSaveable { mutableStateOf("") }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        TopBannerAd()

                        Spacer(Modifier.weight(1f))

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = text,
                            onValueChange = { text = it },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBannerAd(
    modifier: Modifier = Modifier,
    adId: String = "ca-app-pub-3940256099942544/9214589741",
    loadAd: Boolean = true
) {
    val deviceWidth =
        LocalConfiguration.current.screenWidthDp.toInt()

    val context = LocalContext.current

    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, deviceWidth)
    val adRequest = BannerAdRequest.Builder(adId, adSize).build()

    val adView = remember(deviceWidth) {
        AdView(context).apply {
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }
    if (loadAd)
        LaunchedEffect(adView) {
            adView.loadAd(
                adRequest,
                object : AdLoadCallback<BannerAd> {
                    override fun onAdLoaded(ad: BannerAd) {
                        ad.adEventCallback =
                            object : BannerAdEventCallback {
                                override fun onAdImpression() {}

                                override fun onAdClicked() {}
                            }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {}
                },
            )
        }

    DisposableEffect(adView) {
        onDispose {
            adView.destroy()
        }
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Red),
        factory = {
            adView
            /* TextView(it).apply {
                 text = "Banner"
             }*/
        }
    )
}