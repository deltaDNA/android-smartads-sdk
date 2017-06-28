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
import com.deltadna.android.sdk.ads.provider.admob.AdMobInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.admob.AdMobRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter;
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.facebook.FacebookInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter;
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter;
import com.deltadna.android.sdk.ads.provider.tapjoy.TapjoyAdapter;
import com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

enum AdProvider {
    
    ADCOLONY(
            "com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter",
            "com.adcolony.sdk") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.adcolony.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    ADMOB(  "com.deltadna.android.sdk.ads.provider.admob.AdMobInterstitialAdapter",
            "com.google.android.gms.ads") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AdMobInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("adUnitId"));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.admob.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return ADMOB_REWARDED;
        }
    },
    
    ADMOB_REWARDED(
            "com.deltadna.android.sdk.ads.provider.admob.AdMobRewardedAdapter",
            ADMOB.namespace) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new AdMobRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("adUnitId"));
        }
        
        @Override
        String version() {
            return ADMOB.version();
        }
    },
    
    AMAZON( "com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter",
            "com.amazon.device.ads") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.amazon.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    APPLOVIN(
            "com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter",
            "com.applovin.sdk") {
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
                    config.optString("placement"),
                    config.optBoolean("verboseLogging", false));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.applovin.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    CHARTBOOST(
            "com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostInterstitialAdapter",
            "com.chartboost.sdk") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.chartboost.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return CHARTBOOST_REWARDED;
        }
    },
    
    CHARTBOOST_REWARDED(
            "com.deltadna.android.sdk.ads.provider.chartboost.ChartBoostRewardedAdapter",
            CHARTBOOST.namespace) {
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
        
        @Override
        String version() {
            return CHARTBOOST.version();
        }
    },
    
    FACEBOOK(
            "com.deltadna.android.sdk.ads.provider.facebook.FacebookInterstitialAdapter",
            "com.facebook.ads") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new FacebookInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("placementId"));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.facebook.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    FLURRY( "com.deltadna.android.sdk.ads.provider.flurry.FlurryInterstitialAdapter",
            "com.flurry.android") {
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
                    config.optBoolean("testMode", false),
                    false);
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.flurry.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return FLURRY_REWARDED;
        }
    },
    
    FLURRY_REWARDED(
            "com.deltadna.android.sdk.ads.provider.flurry.FlurryRewardedAdapter",
            FLURRY.namespace) {
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
                    config.optBoolean("testMode", false),
                    false);
        }
        
        @Override
        String version() {
            return FLURRY.version();
        }
    },
    
    INMOBI( "com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter",
            "com.inmobi.sdk") {
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
        
        @Override
        boolean present() {
            return false; // SA-94
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.inmobi.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return INMOBI_REWARDED;
        }
    },
    
    INMOBI_REWARDED(
            "com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter",
            INMOBI.namespace) {
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
        
        @Override
        boolean present() {
            return false; // SA-94
        }
        
        @Override
        String version() {
            return INMOBI.version();
        }
    },
    
    IRONSOURCE(
            "com.deltadna.android.sdk.ads.provider.ironsource.IronSourceInterstitialAdapter",
            "com.ironsource.mediationsdk") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new IronSourceInterstitialAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appKey"),
                    false);
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.ironsource.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return IRONSOURCE_REWARDED;
        }
    },
    
    IRONSOURCE_REWARDED(
            "com.deltadna.android.sdk.ads.provider.ironsource.IronSourceRewardedAdapter",
            IRONSOURCE.namespace) {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new IronSourceRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("appKey"),
                    false);
        }
        
        @Override
        String version() {
            return IRONSOURCE.version();
        }
    },
    
    MOBFOX( "com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter",
            "com.mobfox.sdk.interstitialads") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.mobfox.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    MOPUB(  "com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter",
            "com.mopub.mobileads") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.mopub.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    TAPJOY( "com.deltadna.android.sdk.ads.provider.tapjoy.TapjoyAdapter",
            "com.tapjoy") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new TapjoyAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("sdkKey"),
                    config.getString("placementName"),
                    config.optBoolean("logging", false));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.tapjoy.BuildConfig.PROVIDER_VERSION;
        }
        
        @Nullable
        @Override
        AdProvider rewarded() {
            return this;
        }
    },
    
    THIRDPRESENCE(
            "com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceRewardedAdapter",
            "com.thirdpresence.adsdk.sdk") {
        @Override
        MediationAdapter createAdapter(
                int eCPM,
                int adFloorPrice,
                int demoteOnCode,
                int index,
                JSONObject config) throws JSONException {
            
            return new ThirdPresenceRewardedAdapter(
                    eCPM,
                    demoteOnCode,
                    index,
                    config.getString("accountName"),
                    config.getString("placementId"),
                    config.optBoolean("testMode"));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.thirdpresence.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    UNITY(  "com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter",
            "com.unity3d.ads") {
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
                    config.getString("placementId"),
                    config.optBoolean("testMode", false));
        }
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.unity.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    VUNGLE( "com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter",
            "com.vungle.publisher") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.provider.vungle.BuildConfig.PROVIDER_VERSION;
        }
    },
    
    DUMMY(  "com.deltadna.android.sdk.ads.provider.dummy",
            "com.deltadna.android.sdk.ads.provider.dummy") {
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
        
        @Override
        String version() {
            return com.deltadna.android.sdk.ads.core.BuildConfig.VERSION_NAME;
        }
    };
    
    public final String cls;
    public final String namespace;
    
    AdProvider(String cls, String namespace) {
        this.cls = cls;
        this.namespace = namespace;
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
     * @return the rewarded ad provider, may be {@code null} if not present
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
    
    abstract String version();
     
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
    
    static AdProvider defines(MediationAdapter adapter) {
        for (final AdProvider provider : values()) {
            if (provider.cls.equals(adapter.getClass().getName())) {
                return provider;
            }
        }
        
        return DUMMY;
    }
}
