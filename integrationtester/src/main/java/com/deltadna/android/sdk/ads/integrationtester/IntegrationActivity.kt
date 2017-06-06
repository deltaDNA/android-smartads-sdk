/*
 * Copyright (c) 2016 deltaDNA Ltd. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deltadna.android.sdk.ads.integrationtester

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter
import com.deltadna.android.sdk.ads.provider.admob.AdMobInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.admob.AdMobRewardedAdapter
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter
import com.deltadna.android.sdk.ads.provider.facebook.FacebookInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceRewardedAdapter
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter
import com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceRewardedAdapter
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter
import org.json.JSONObject

class IntegrationActivity : AppCompatActivity() {
    
    private val providers by lazy {
        findViewById(R.id.integration_providers) as ListView
    }
    
    private var provider: MediationAdapter = PROVIDERS[0]
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_integration)
        
        providers.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_single_choice,
                PROVIDER_NAMES)
        providers.setOnItemClickListener { _, _, position, _ ->
            provider = PROVIDERS[position]
            debug("Using ${provider.javaClass.simpleName}")
        }
        providers.setItemChecked(0, true)
    }
    
    fun onRequestAd(view: View) {
        provider.requestAd(this, Listener(), JSONObject())
    }
    
    fun onShowAd(view: View) {
        provider.showAd()
    }
    
    private inner class Listener : MediationListener {
        
        override fun onAdLoaded(mediationAdapter: MediationAdapter) {
            debug("Ad loaded")
        }
        
        override fun onAdFailedToLoad(
                mediationAdapter: MediationAdapter,
                adLoadResult: AdRequestResult,
                reason: String) {
            
            debug("Ad failed to load; result: $adLoadResult $reason")
        }
        
        override fun onAdShowing(mediationAdapter: MediationAdapter) {
            debug("Ad showing")
        }
        
        override fun onAdFailedToShow(
                mediationAdapter: MediationAdapter,
                adClosedResult: AdClosedResult) {
            
            debug("Ad failed to show; result: $adClosedResult")
        }
        
        override fun onAdClicked(mediationAdapter: MediationAdapter) {
            debug("Ad clicked")
        }
        
        override fun onAdLeftApplication(mediationAdapter: MediationAdapter) {
            debug("Ad left application")
        }
        
        override fun onAdClosed(
                mediationAdapter: MediationAdapter,
                complete: Boolean) {
            
            debug("Ad closed; complete: $complete")
        }
    }
    
    private companion object {
        
        val PROVIDERS = listOf(
                AdColonyAdapter(
                        0,
                        0,
                        0,
                        "appc804f742b8064114a9",
                        "vzbb9fa7accb4e4185b7"),
                AdMobInterstitialAdapter(
                        0,
                        0,
                        0,
                        "ca-app-pub-3940256099942544/1033173712"),
                AdMobRewardedAdapter(
                        0,
                        0,
                        0,
                        "ca-app-pub-3940256099942544/5224354917"),
                AmazonAdapter(
                        0,
                        0,
                        0,
                        "b156d556c85c4a918be92ee218708d4a",
                        true),
                AppLovinRewardedAdapter(
                        0,
                        0,
                        0,
                        "ElG63iTpOQfZvG4kizCGhhXZQiWt37hIszOvfyi3MNdFdh-KeAbKt7vHrQ9uXrBNpZHTV-WtL87-r6IUGvp80h",
                        "Interstitial",
                        true),
                ChartBoostInterstitialAdapter(
                        0,
                        0,
                        0,
                        "58f489a743150f4385b20df2",
                        "39eb54cb811959e303cacd9ccc6e9360d8a7b424",
                        ChartBoostInterstitialAdapter.LOCATION),
                ChartBoostRewardedAdapter(
                        0,
                        0,
                        0,
                        "58f489a743150f4385b20df2",
                        "39eb54cb811959e303cacd9ccc6e9360d8a7b424",
                        ChartBoostRewardedAdapter.LOCATION),
                FacebookInterstitialAdapter(
                        0,
                        0,
                        0,
                        "1296985967048898_1296988693715292"),
                FlurryInterstitialAdapter(
                        0,
                        0,
                        0,
                        "HWJM27QJT2HS2ZBBG32P",
                        "INTERSTITIAL_ADSPACE",
                        true,
                        true),
                FlurryRewardedAdapter(
                        0,
                        0,
                        0,
                        "HWJM27QJT2HS2ZBBG32P",
                        "REWARDED_ADSPACE",
                        true,
                        true),
                InMobiInterstitialAdapter(
                        0,
                        0,
                        0,
                        "d9518d128a124772b07a750fa98d1bbe",
                        1447292217917L),
                InMobiRewardedAdapter(
                        0,
                        0,
                        0,
                        "d9518d128a124772b07a750fa98d1bbe",
                        1464837141675L),
                IronSourceInterstitialAdapter(
                        0,
                        0,
                        0,
                        "6114d36d",
                        true),
                IronSourceRewardedAdapter(
                        0,
                        0,
                        0,
                        "6114d36d",
                        true),
                MobFoxAdapter(
                        0,
                        0,
                        0,
                        "303fc0e182f1e126f276537f2b3d01ee"),
                MoPubAdapter(
                        0,
                        0,
                        0,
                        "3ac456d99b6a4cffb58cfba31fab2a21"),
                ThirdPresenceRewardedAdapter(
                        0,
                        0,
                        0,
                        "tpr-deltadna",
                        "lkqa0wifqs",
                        true),
                UnityRewardedAdapter(
                        0,
                        0,
                        0,
                        "109764",
                        null,
                        true),
                VungleAdapter(
                        0,
                        0,
                        0,
                        "5832df18d614b1ab17000251"))
        val PROVIDER_NAMES = PROVIDERS.map { it.javaClass.simpleName }
        
        fun debug(msg: String) {
            Log.d(IntegrationActivity::class.java.simpleName, msg)
        }
    }
}
