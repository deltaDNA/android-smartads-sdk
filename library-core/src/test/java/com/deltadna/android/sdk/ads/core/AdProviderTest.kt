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

package com.deltadna.android.sdk.ads.core

import com.deltadna.android.sdk.ads.core.AdProvider.*
import com.deltadna.android.sdk.ads.core.AdProviderType.INTERSTITIAL
import com.deltadna.android.sdk.ads.core.AdProviderType.REWARDED
import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter
import com.deltadna.android.sdk.ads.provider.supersonic.SupersonicInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.supersonic.SupersonicRewardedAdapter
import com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceRewardedAdapter
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter
import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AdProviderTest {
    
    @Test
    fun adColony() {
        assertThat(ADCOLONY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("clientOptions", "clientOptions")
                        .put("zoneId", "zoneId")))
                .isInstanceOf(AdColonyAdapter::class.java)
    }
    
    @Test
    fun adMob() {
        assertThat(ADMOB.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("adUnitId", "adUnitId")))
                .isInstanceOf(AdMobAdapter::class.java)
    }
    
    @Test
    fun amazon() {
        assertThat(AMAZON.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appKey", "appKey")
                        .put("testMode", true)))
                .isInstanceOf(AmazonAdapter::class.java)
    }
    
    @Test
    fun appLovin() {
        assertThat(APPLOVIN.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("sdkKey", "sdkKey")
                        .put("placement", "placement")
                        .put("verboseLogging", true)
                        .put("adRefreshSeconds", 1)))
                .isInstanceOf(AppLovinRewardedAdapter::class.java)
    }
    
    @Test
    fun chartboost() {
        assertThat(CHARTBOOST.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature")))
                .isInstanceOf(ChartBoostInterstitialAdapter::class.java)
    }
    
    @Test
    fun chartboostRewarded() {
        assertThat(CHARTBOOST_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature")))
                .isInstanceOf(ChartBoostRewardedAdapter::class.java)
        
        assertThat(CHARTBOOST.rewarded()).isSameAs(CHARTBOOST_REWARDED)
    }
    
    @Test
    fun flurry() {
        assertThat(FLURRY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true)))
                .isInstanceOf(FlurryInterstitialAdapter::class.java)
    }
    
    @Test
    fun flurryRewarded() {
        assertThat(FLURRY_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true)))
                .isInstanceOf(FlurryRewardedAdapter::class.java)
        
        assertThat(FLURRY.rewarded()).isSameAs(FLURRY_REWARDED)
    }
    
    @Test
    fun inMobi() {
        assertThat(INMOBI.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L)))
                .isInstanceOf(InMobiInterstitialAdapter::class.java)
    }
    
    @Test
    fun inMobiRewarded() {
        assertThat(INMOBI_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L)))
                .isInstanceOf(InMobiRewardedAdapter::class.java)
        
        assertThat(INMOBI.rewarded()).isSameAs(INMOBI_REWARDED)
    }
    
    @Test
    fun mobFox() {
        assertThat(MOBFOX.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("publicationId", "publicationId")))
                .isInstanceOf(MobFoxAdapter::class.java)
    }
    
    @Test
    fun moPub() {
        assertThat(MOPUB.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("adUnitId", "adUnitId")))
                .isInstanceOf(MoPubAdapter::class.java)
    }
    
    @Test
    fun supersonic() {
        assertThat(SUPERSONIC.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("appKey", "appKey").put("log", true)))
                .isInstanceOf(SupersonicInterstitialAdapter::class.java)
    }
    
    @Test
    fun supersonicRewarded() {
        assertThat(SUPERSONIC_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("appKey", "appKey").put("log", true)))
                .isInstanceOf(SupersonicRewardedAdapter::class.java)
        
        assertThat(SUPERSONIC.rewarded()).isSameAs(SUPERSONIC_REWARDED)
    }
    
    @Test
    fun thirdpresence() {
        assertThat(THIRDPRESENCE.createAdapter(
                1,
                2,
                3,
                4,
                JSONObject()
                        .put("accountName", "accountName")
                        .put("placementId", "placementId")
                        .put("testMode", true)))
                .isInstanceOf(ThirdPresenceRewardedAdapter::class.java)
    }
    
    @Test
    fun unity() {
        assertThat(UNITY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("gameId", "gameId")
                        .put("zoneId", "zoneId")
                        .put("testMode", true)))
                .isInstanceOf(UnityRewardedAdapter::class.java)
    }
    
    @Test
    fun vungle() {
        assertThat(VUNGLE.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("appId", "appId")))
                .isInstanceOf(VungleAdapter::class.java)
    }
    
    @Test
    fun valueOfInterstitial() {
        assertThat(valueOf(provider("ADCOLONY"), INTERSTITIAL))
                .isSameAs(ADCOLONY)
        assertThat(valueOf(provider("adcolony"), INTERSTITIAL))
                .isSameAs(ADCOLONY)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun valueOfInterstitialInvalid() {
        valueOf(provider("badcolony"), INTERSTITIAL)
    }
    
    @Test
    fun valueOfRewarded() {
        assertThat(valueOf(provider("CHARTBOOST"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED)
        assertThat(valueOf(provider("chartboost"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED)
        
        assertThat(valueOf(provider("CHARTBOOST-REWARDED"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED)
        assertThat(valueOf(provider("chartboost-rewarded"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED)
    }
    
    @Test
    fun valueOfRewardedInvalid() {
        valueOf(provider("adcolony"), REWARDED)
    }
    
    private fun provider(name: String) = JSONObject().put("adProvider", name)
}
