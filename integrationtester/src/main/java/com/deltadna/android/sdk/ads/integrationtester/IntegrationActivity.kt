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
                        BuildConfig.ADCOLONY_APP_ID,
                        BuildConfig.ADCOLONY_ZONE_IDS),
                AdMobInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID),
                AdMobRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.ADMOB_REWARDED_AD_UNIT_ID),
                AmazonAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.AMAZON_APP_KEY,
                        true),
                AppLovinRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.APPLOVIN_KEY,
                        "Interstitial",
                        true),
                ChartBoostInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.CHARTBOOST_APP_ID,
                        BuildConfig.CHARTBOOST_APP_SIGNATURE,
                        ChartBoostInterstitialAdapter.LOCATION),
                ChartBoostRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.CHARTBOOST_APP_ID,
                        BuildConfig.CHARTBOOST_APP_SIGNATURE,
                        ChartBoostRewardedAdapter.LOCATION),
                FacebookInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.FACEBOOK_PLACEMENT_ID),
                FlurryInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.FLURRY_API_KEY,
                        "INTERSTITIAL_ADSPACE",
                        true,
                        true),
                FlurryRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.FLURRY_API_KEY,
                        "REWARDED_ADSPACE",
                        true,
                        true),
                InMobiInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.INMOBI_ACCOUNT_ID,
                        BuildConfig.INMOBI_INTERSTITIAL_PLACEMENT_ID),
                InMobiRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.INMOBI_ACCOUNT_ID,
                        BuildConfig.INMOBI_REWARDED_PLACEMENT_ID),
                IronSourceInterstitialAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.IRONSOURCE_APP_KEY,
                        true),
                IronSourceRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.IRONSOURCE_APP_KEY,
                        true),
                MobFoxAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.MOBFOX_PUBLICATION_ID),
                MoPubAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.MOPUB_AD_UNIT_ID),
                ThirdPresenceRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.THIRDPRESENCE_ACCOUNT_NAME,
                        BuildConfig.THIRDPRESENCE_PLACEMENT_ID,
                        true),
                UnityRewardedAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.UNITY_GAME_ID,
                        "defaultZone",
                        true),
                VungleAdapter(
                        0,
                        0,
                        0,
                        BuildConfig.VUNGLE_APP_ID))
        val PROVIDER_NAMES = PROVIDERS.map { it.javaClass.simpleName }
        
        fun debug(msg: String) {
            Log.d(IntegrationActivity::class.java.simpleName, msg)
        }
    }
}
