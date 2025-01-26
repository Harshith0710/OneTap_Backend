package com.example.geminiapikey

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.input.key.type
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class HomeActivity : ComponentActivity() {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    val REQUEST_CAMERA = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load the interstitial and rewarded ads
        loadInterstitialAd()
        loadRewardedAd()

        setContent {
            HomeScreen { action ->
                handleActionButtonClick(action)
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // Replace with your Ad Unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-3940256099942544/5224354917", // Replace with your Ad Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        // Show interstitial ad when app is reopened
        showInterstitialAd()
    }

    private fun showInterstitialAd() {
        interstitialAd?.let {
            it.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Reload the ad after it's shown
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Reload the ad if it fails to show
                    loadInterstitialAd()
                }
            }
            it.show(this)
        } ?: loadInterstitialAd() // Reload if no ad is loaded
    }

    private fun handleActionButtonClick(action: String) {
        val intent = when (action[0]) {
            'C' -> Intent(this, BakingActivity::class.java)
            'T' -> Intent(this, SpeakActivity::class.java)
            'S' -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, REQUEST_CAMERA)
                return
            }
            else -> null
        }
        intent?.let {
            showRewardedAd { // Show rewarded ad before starting the activity
                startActivity(it)
            }
        }
    }

    private fun showRewardedAd(onAdCompleted: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Ad was dismissed, proceed with the action
                    loadRewardedAd() // Reload the ad
                    onAdCompleted()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Ad failed to show, proceed directly
                    loadRewardedAd() // Reload the ad
                    onAdCompleted()
                }
            }
            ad.show(this) { rewardItem ->
                // Handle the reward if needed
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
            }
        } ?: run {
            // If no ad is loaded, proceed directly
            onAdCompleted()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                // Pass the image to another activity
                val intent = Intent(this, BakingActivity::class.java)
                intent.putExtra("image", imageBitmap)
                startActivity(intent)
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Exit the app when the back button is pressed
        finishAffinity() // Closes all activities and exits the app
    }
}
