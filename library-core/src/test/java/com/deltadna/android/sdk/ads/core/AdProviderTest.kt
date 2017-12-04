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
import com.deltadna.android.sdk.ads.provider.admob.AdMobInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.admob.AdMobRewardedAdapter
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter
import com.deltadna.android.sdk.ads.provider.facebook.FacebookInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.facebook.FacebookRewardedAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter
import com.deltadna.android.sdk.ads.provider.hyprmx.HyprMxAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceRewardedAdapter
import com.deltadna.android.sdk.ads.provider.loopme.LoopMeAdapter
import com.deltadna.android.sdk.ads.provider.machinezone.MachineZoneInterstitialAdapter
import com.deltadna.android.sdk.ads.provider.machinezone.MachineZoneRewardedAdapter
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter
import com.deltadna.android.sdk.ads.provider.tapjoy.TapjoyAdapter
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
        with(ADCOLONY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("clientOptions", "clientOptions")
                        .put("zoneId", "zoneId"))) {
            assertThat(this).isInstanceOf(AdColonyAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(ADCOLONY.version())
            assertThat(this.javaClass.name).isEqualTo(ADCOLONY.cls)
        }
    }
    
    @Test
    fun adMob() {
        with(ADMOB.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("adUnitId", "adUnitId")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(AdMobInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(ADMOB.version())
            assertThat(this.javaClass.name).isEqualTo(ADMOB.cls)
        }
    }
    
    @Test
    fun adMobRewarded() {
        with(ADMOB_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("adUnitId", "adUnitId")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(AdMobRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(ADMOB_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(ADMOB_REWARDED.cls)
        }
        
        assertThat(ADMOB.rewarded()).isSameAs(ADMOB_REWARDED)
    }
    
    @Test
    fun amazon() {
        with(AMAZON.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appKey", "appKey")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(AmazonAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(AMAZON.version())
            assertThat(this.javaClass.name).isEqualTo(AMAZON.cls)
        }
    }
    
    @Test
    fun appLovin() {
        with(APPLOVIN.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("sdkKey", "sdkKey")
                        .put("placement", "placement")
                        .put("verboseLogging", true)
                        .put("adRefreshSeconds", 1))) {
            assertThat(this).isInstanceOf(AppLovinRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(APPLOVIN.version())
            assertThat(this.javaClass.name).isEqualTo(APPLOVIN.cls)
        }
    }
    
    @Test
    fun chartboost() {
        with(CHARTBOOST.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature"))) {
            assertThat(this).isInstanceOf(ChartBoostInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(CHARTBOOST.version())
            assertThat(this.javaClass.name).isEqualTo(CHARTBOOST.cls)
        }
    }
    
    @Test
    fun chartboostRewarded() {
        with(CHARTBOOST_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature"))) {
            assertThat(this).isInstanceOf(ChartBoostRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(CHARTBOOST_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(CHARTBOOST_REWARDED.cls)
        }
        
        assertThat(CHARTBOOST.rewarded()).isSameAs(CHARTBOOST_REWARDED)
    }
    
    @Test
    fun facebook() {
        with(FACEBOOK.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("placementId", "placement"))) {
            assertThat(this).isInstanceOf(FacebookInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(FACEBOOK.version())
            assertThat(this.javaClass.name).isEqualTo(FACEBOOK.cls)
        }
    }
    
    @Test
    fun facebookRewarded() {
        with(FACEBOOK_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("placementId", "placement"))) {
            assertThat(this).isInstanceOf(FacebookRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(FACEBOOK_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(FACEBOOK_REWARDED.cls)
        }
        
        assertThat(FACEBOOK.rewarded()).isSameAs(FACEBOOK_REWARDED)
    }
    
    @Test
    fun flurry() {
        with(FLURRY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(FlurryInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(FLURRY.version())
            assertThat(this.javaClass.name).isEqualTo(FLURRY.cls)
        }
    }
    
    @Test
    fun flurryRewarded() {
        with(FLURRY_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(FlurryRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(FLURRY_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(FLURRY_REWARDED.cls)
        }
        
        assertThat(FLURRY.rewarded()).isSameAs(FLURRY_REWARDED)
    }
    
    @Test
    fun hyprmx() {
        with(HYPRMX.createAdapter(
                1,
                2,
                3,
                4,
                JSONObject()
                        .put("distributorId", "distributorId")
                        .put("propertyId", "propertyId"))) {
            assertThat(this).isInstanceOf(HyprMxAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(HYPRMX.version())
            assertThat(this.javaClass.name).isEqualTo(HYPRMX.cls)
        }
    }
    
    @Test
    fun inMobi() {
        with(INMOBI.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L))) {
            assertThat(this).isInstanceOf(InMobiInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(INMOBI.version())
            assertThat(this.javaClass.name).isEqualTo(INMOBI.cls)
        }
    }
    
    @Test
    fun inMobiRewarded() {
        with(INMOBI_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L))) {
            assertThat(this).isInstanceOf(InMobiRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(INMOBI_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(INMOBI_REWARDED.cls)
        }
        
        assertThat(INMOBI.rewarded()).isSameAs(INMOBI_REWARDED)
    }
    
    @Test
    fun ironSource() {
        with(IRONSOURCE.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appKey", "appKey")
                        .put("placementName", "placementName"))) {
            assertThat(this).isInstanceOf(IronSourceInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(IRONSOURCE.version())
            assertThat(this.javaClass.name).isEqualTo(IRONSOURCE.cls)
        }
    }
    
    @Test
    fun ironSourceRewarded() {
        with(IRONSOURCE_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appKey", "appKey")
                        .put("placementName", "placementName"))) {
            assertThat(this).isInstanceOf(IronSourceRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(IRONSOURCE_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(IRONSOURCE_REWARDED.cls)
        }
        
        assertThat(IRONSOURCE.rewarded()).isSameAs(IRONSOURCE_REWARDED)
    }
    
    @Test
    fun loopMe() {
        with(LOOPME.createAdapter(
                1,
                2,
                3,
                4,
                JSONObject()
                        .put("appKey", "appKey")
                        .put("testMode", false))) {
            assertThat(this).isInstanceOf(LoopMeAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(LOOPME.version())
            assertThat(this.javaClass.name).isEqualTo(LOOPME.cls)
        }
    }
    
    @Test
    fun machineZone() {
        with(MACHINEZONE.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("adUnitId", "adUnitId"))) {
            assertThat(this).isInstanceOf(MachineZoneInterstitialAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(MACHINEZONE.version())
            assertThat(this.javaClass.name).isEqualTo(MACHINEZONE.cls)
        }
    }
    
    @Test
    fun machineZoneRewarded() {
        with(MACHINEZONE_REWARDED.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("adUnitId", "adUnitId"))) {
            assertThat(this).isInstanceOf(MachineZoneRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(MACHINEZONE_REWARDED.version())
            assertThat(this.javaClass.name).isEqualTo(MACHINEZONE_REWARDED.cls)
        }
        
        assertThat(MACHINEZONE.rewarded()).isSameAs(MACHINEZONE_REWARDED)
    }
    
    @Test
    fun mobFox() {
        with(MOBFOX.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("publicationId", "publicationId"))) {
            assertThat(this).isInstanceOf(MobFoxAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(MOBFOX.version())
            assertThat(this.javaClass.name).isEqualTo(MOBFOX.cls)
        }
    }
    
    @Test
    fun moPub() {
        with(MOPUB.createAdapter(
                1, 2, 3, 4,
                JSONObject().put("adUnitId", "adUnitId"))) {
            assertThat(this).isInstanceOf(MoPubAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(MOPUB.version())
            assertThat(this.javaClass.name).isEqualTo(MOPUB.cls)
        }
    }
    
    @Test
    fun tapjoy() {
        with(TAPJOY.createAdapter(
                1,
                2,
                3,
                4,
                JSONObject()
                        .put("sdkKey", "sdkKey")
                        .put("placementName", "placementName")
                        .put("logging", true))) {
            assertThat(this).isInstanceOf(TapjoyAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(TAPJOY.version())
            assertThat(this.javaClass.name).isEqualTo(TAPJOY.cls)
        }
    }
    
    @Test
    fun thirdpresence() {
        with(THIRDPRESENCE.createAdapter(
                1,
                2,
                3,
                4,
                JSONObject()
                        .put("accountName", "accountName")
                        .put("placementId", "placementId")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(ThirdPresenceRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(THIRDPRESENCE.version())
            assertThat(this.javaClass.name).isEqualTo(THIRDPRESENCE.cls)
        }
    }
    
    @Test
    fun unity() {
        with(UNITY.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("gameId", "gameId")
                        .put("placementId", "placementId")
                        .put("testMode", true))) {
            assertThat(this).isInstanceOf(UnityRewardedAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(UNITY.version())
            assertThat(this.javaClass.name).isEqualTo(UNITY.cls)
        }
    }
    
    @Test
    fun vungle() {
        with(VUNGLE.createAdapter(
                1, 2, 3, 4,
                JSONObject()
                        .put("appId", "appId")
                        .put("placementId", "placementId"))) {
            assertThat(this).isInstanceOf(VungleAdapter::class.java)
            assertThat(this.providerVersionString).isEqualTo(VUNGLE.version())
            assertThat(this.javaClass.name).isEqualTo(VUNGLE.cls)
        }
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
    
    @Test
    fun defines() {
        assertThat(defines(AdMobInterstitialAdapter(0, 0, 0, "", "", false)))
                .isEqualTo(ADMOB)
        assertThat(defines(IronSourceRewardedAdapter(0, 0, 0, "", "", false)))
                .isEqualTo(IRONSOURCE_REWARDED)
    }
    
    private fun provider(name: String) = JSONObject().put("adProvider", name)
}
