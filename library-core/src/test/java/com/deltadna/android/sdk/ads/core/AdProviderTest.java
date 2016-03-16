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

package com.deltadna.android.sdk.ads.core;

import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter;
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter;
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter;
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter;
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter;
import com.deltadna.android.sdk.ads.provider.supersonic.SupersonicInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.supersonic.SupersonicRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.deltadna.android.sdk.ads.core.AdProvider.ADCOLONY;
import static com.deltadna.android.sdk.ads.core.AdProvider.ADMOB;
import static com.deltadna.android.sdk.ads.core.AdProvider.AMAZON;
import static com.deltadna.android.sdk.ads.core.AdProvider.CHARTBOOST;
import static com.deltadna.android.sdk.ads.core.AdProvider.CHARTBOOST_REWARDED;
import static com.deltadna.android.sdk.ads.core.AdProvider.FLURRY;
import static com.deltadna.android.sdk.ads.core.AdProvider.FLURRY_REWARDED;
import static com.deltadna.android.sdk.ads.core.AdProvider.INMOBI;
import static com.deltadna.android.sdk.ads.core.AdProvider.INMOBI_REWARDED;
import static com.deltadna.android.sdk.ads.core.AdProvider.MOBFOX;
import static com.deltadna.android.sdk.ads.core.AdProvider.MOPUB;
import static com.deltadna.android.sdk.ads.core.AdProvider.SUPERSONIC;
import static com.deltadna.android.sdk.ads.core.AdProvider.SUPERSONIC_REWARDED;
import static com.deltadna.android.sdk.ads.core.AdProvider.UNITY;
import static com.deltadna.android.sdk.ads.core.AdProvider.VUNGLE;
import static com.deltadna.android.sdk.ads.core.AdProvider.valueOf;
import static com.deltadna.android.sdk.ads.core.AdProviderType.INTERSTITIAL;
import static com.deltadna.android.sdk.ads.core.AdProviderType.REWARDED;
import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class AdProviderTest {
    
    @Test
    public void adColony() throws JSONException {
        assertThat(ADCOLONY.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("appId", "appId")
                        .put("clientOptions", "clientOptions")
                        .put("zoneId", "zoneId")))
                .isInstanceOf(AdColonyAdapter.class);
    }
    
    @Test
    public void adMob() throws JSONException {
        assertThat(ADMOB.createAdapter(
                1, 2, 3, 4,
                new JSONObject().put("adUnitId", "adUnitId")))
                .isInstanceOf(AdMobAdapter.class);
    }
    
    @Test
    public void amazon() throws JSONException {
        assertThat(AMAZON.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("appKey", "appKey")
                        .put("testMode", true)))
                .isInstanceOf(AmazonAdapter.class);
    }
    
    @Test
    public void chartboost() throws JSONException {
        assertThat(CHARTBOOST.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature")))
                .isInstanceOf(ChartBoostInterstitialAdapter.class);
    }
    
    @Test
    public void chartboostRewarded() throws JSONException {
        assertThat(CHARTBOOST_REWARDED.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("appId", "appId")
                        .put("appSignature", "appSignature")))
                .isInstanceOf(ChartBoostRewardedAdapter.class);
        
        assertThat(CHARTBOOST.rewarded()).isSameAs(CHARTBOOST_REWARDED);
    }
    
    @Test
    public void flurry() throws JSONException {
        assertThat(FLURRY.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true)))
                .isInstanceOf(FlurryInterstitialAdapter.class);
    }
    
    @Test
    public void flurryRewarded() throws JSONException {
        assertThat(FLURRY_REWARDED.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("apiKey", "apiKey")
                        .put("adSpace", "adSpace")
                        .put("testMode", true)))
                .isInstanceOf(FlurryRewardedAdapter.class);
        
        assertThat(FLURRY.rewarded()).isSameAs(FLURRY_REWARDED);
    }
    
    @Test
    public void inMobi() throws JSONException {
        assertThat(INMOBI.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L)))
                .isInstanceOf(InMobiInterstitialAdapter.class);
    }
    
    @Test
    public void inMobiRewarded() throws JSONException {
        assertThat(INMOBI_REWARDED.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("accountId", "accountId")
                        .put("placementId", 1L)))
                .isInstanceOf(InMobiRewardedAdapter.class);
        
        assertThat(INMOBI.rewarded()).isSameAs(INMOBI_REWARDED);
    }
    
    @Test
    public void mobFox() throws JSONException {
        assertThat(MOBFOX.createAdapter(
                1, 2, 3, 4,
                new JSONObject().put("publicationId", "publicationId")))
                .isInstanceOf(MobFoxAdapter.class);
    }
    
    @Test
    public void moPub() throws JSONException {
        assertThat(MOPUB.createAdapter(
                1, 2, 3, 4,
                new JSONObject().put("adUnitId", "adUnitId")))
                .isInstanceOf(MoPubAdapter.class);
    }
    
    @Test
    public void supersonic() throws JSONException {
        assertThat(SUPERSONIC.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("appKey", "appKey")
                        .put("log", true)))
                .isInstanceOf(SupersonicInterstitialAdapter.class);
    }
    
    @Test
    public void supersonicRewarded() throws JSONException {
        assertThat(SUPERSONIC_REWARDED.createAdapter(
                1,2,3,4,
                new JSONObject()
                        .put("appKey", "appKey")
                        .put("log", true)))
                .isInstanceOf(SupersonicRewardedAdapter.class);
        
        assertThat(SUPERSONIC.rewarded()).isSameAs(SUPERSONIC_REWARDED);
    }
    
    @Test
    public void unity() throws JSONException {
        assertThat(UNITY.createAdapter(
                1, 2, 3, 4,
                new JSONObject()
                        .put("gameId", "gameId")
                        .put("zoneId", "zoneId")
                        .put("testMode", true)))
                .isInstanceOf(UnityRewardedAdapter.class);
    }
    
    @Test
    public void vungle() throws JSONException {
        assertThat(VUNGLE.createAdapter(
                1, 2, 3, 4,
                new JSONObject().put("appId", "appId")))
                .isInstanceOf(VungleAdapter.class);
    }
    
    @Test
    public void valueOfInterstitial() throws JSONException {
        assertThat(valueOf(provider("ADCOLONY"), INTERSTITIAL))
                .isSameAs(ADCOLONY);
        assertThat(valueOf(provider("adcolony"), INTERSTITIAL))
                .isSameAs(ADCOLONY);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void valueOfInterstitialInvalid() throws JSONException {
        valueOf(provider("badcolony"), INTERSTITIAL);
    }
    
    @Test
    public void valueOfRewarded() throws JSONException {
        assertThat(valueOf(provider("CHARTBOOST"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED);
        assertThat(valueOf(provider("chartboost"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED);
        
        assertThat(valueOf(provider("CHARTBOOST-REWARDED"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED);
        assertThat(valueOf(provider("chartboost-rewarded"), REWARDED))
                .isSameAs(CHARTBOOST_REWARDED);
    }
    
    @Test
    public void valueOfRewardedInvalid() throws JSONException {
        valueOf(provider("adcolony"), REWARDED);
    }
    
    private static JSONObject provider(String name) {
        try {
            return new JSONObject().put("adProvider", name);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
