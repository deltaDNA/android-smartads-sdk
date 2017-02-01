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

import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.core.network.DummyAdapter;
import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter;
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter;
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter;
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter;
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
import com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

enum AdProvider {
    
    ADCOLONY("com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AdColonyAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appId"),
                    config.getString("zoneId"));
        }
    },
    
    ADMOB("com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AdMobAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("adUnitId"));
        }
    },
    
    AMAZON("com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AmazonAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appKey"),
                    config.optBoolean("testMode", false));
        }
    },
    
    APPLOVIN("com.deltadna.android.sdk.ads.provider.applovin.AppLovinInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AppLovinInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("sdkKey"),
                    config.optBoolean("verboseLogging", false),
                    config.optLong("adRefreshSeconds", -1));
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return APPLOVIN_REWARDED;
        }
    },
    
    APPLOVIN_REWARDED(APPLOVIN.cls) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AppLovinRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("sdkKey"),
                    config.optBoolean("verboseLogging", false),
                    config.optLong("adRefreshSeconds"));
        }
    },
    
    CHARTBOOST("com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new ChartBoostInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appId"),
                    config.getString("appSignature"),
                    config.optString(
                            "location",
                            ChartBoostInterstitialAdapter.LOCATION));
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return CHARTBOOST_REWARDED;
        }
    },
    
    CHARTBOOST_REWARDED(CHARTBOOST.cls) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new ChartBoostRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appId"),
                    config.getString("appSignature"),
                    config.optString(
                            "location",
                            ChartBoostInterstitialAdapter.LOCATION));
        }
    },
    
    FLURRY("com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new FlurryInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("apiKey"),
                    config.getString("adSpace"),
                    config.optBoolean("testMode", false));
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return FLURRY_REWARDED;
        }
    },
    
    FLURRY_REWARDED(FLURRY.cls) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new FlurryRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("apiKey"),
                    config.getString("adSpace"),
                    config.optBoolean("testMode", false));
        }
    },
    
    INMOBI("com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new InMobiInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("accountId"),
                    config.getLong("placementId"));
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return INMOBI_REWARDED;
        }
    },
    
    INMOBI_REWARDED(INMOBI.cls) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new InMobiRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("accountId"),
                    config.getLong("placementId"));
        }
    },
    
    MOBFOX("com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new MobFoxAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("publicationId"));
        }
    },
    
    MOPUB("com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new MoPubAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("adUnitId"));
        }
    },
    
    SUPERSONIC("com.deltadna.android.sdk.ads.provider.supersonic.SupersonicInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new SupersonicInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appKey"),
                    config.getBoolean("log"));
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return SUPERSONIC_REWARDED;
        }
    },
    
    SUPERSONIC_REWARDED(SUPERSONIC.cls) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new SupersonicRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appKey"),
                    config.getBoolean("log"));
        }
    },
    
    THIRDPRESENCE("com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceInterstitialAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new ThirdPresenceInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("accountName"),
                    config.getString("placementId"),
                    config.optBoolean("testMode"));
        }
    },
    
    UNITY("com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new UnityRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("gameId"),
                    config.optString("zoneId", null),
                    config.optBoolean("testMode", false));
        }
    },
    
    VUNGLE("com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new VungleAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appId"));
        }
    },
    
    DUMMY(null) {
        @Override
        boolean present() {
            return true;
        }
        
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new DummyAdapter(eCPM, demoteOnCode, index);
        }
    };
    
    private final String cls;
    
    AdProvider(String cls) {
        this.cls = cls;
    }
    
    boolean present() {
        try {
            Class.forName(cls);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    final String legacyName() {
        return name().replace('_', '-');
    }
    
    /**
     * Gets the rewarded counterpart of this provider.
     *
     * @return the rewarded ad provider
     */
    @Nullable
    AdProvider rewarded() {
        return null;
    }
    
    abstract MediationAdapter createAdapter(
            int eCPM,
            int adFloorPrice,
            int demoteOnCode,
            int index,
            JSONObject config) throws JSONException;
     
    static AdProvider valueOf(JSONObject config, AdProviderType type)
            throws JSONException {
        
        final String provider = config.getString("adProvider")
                .toUpperCase(Locale.US);
        
        for (final AdProvider value : values()) {
            if (value.name().equals(provider)) {
                if (    type == AdProviderType.REWARDED
                        && value.rewarded() != null) {
                    
                    return value.rewarded();
                } else {
                    return value;
                }
            }
            
            // handles old-style naming convention
            if (value.legacyName().equals(provider)) {
                return value;
            }
        }
        
        throw new IllegalArgumentException(
                "Provider " + provider + " not found");
    }
}
