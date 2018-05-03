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

package com.deltadna.android.sdk.ads.provider.mopub;

import android.app.Activity;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.mopub.mobileads.MoPubInterstitial;

import org.json.JSONObject;

public final class MoPubAdapter extends MediationAdapter {
    
    private final String adUnitId;
    private final boolean testMode;
    
    private MoPubInterstitial interstitial;
    
    public MoPubAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String adUnitId,
            boolean testMode) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.adUnitId = adUnitId;
        this.testMode = testMode;
    }
    
    @Override
    public void requestAd(final Activity activity, final MediationListener listener, JSONObject configuration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    interstitial = new MoPubInterstitial(activity, adUnitId);
                    interstitial.setTesting(testMode);
                    interstitial.setInterstitialAdListener(new MoPubEventForwarder(listener, MoPubAdapter.this));
                } catch (Exception e) {
                    Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                    listener.onAdFailedToLoad(
                            MoPubAdapter.this,
                            AdRequestResult.Configuration,
                            "Invalid MoPub configuration: " + e);
                }
                
                try {
                    interstitial.load();
                } catch (Exception e) {
                    Log.e(BuildConfig.LOG_TAG, "Failed to request ad", e);
                    listener.onAdFailedToLoad(
                            MoPubAdapter.this,
                            AdRequestResult.Error,
                            "Failed to request MoPub ad: " + e);
                }
            }
        });
    }
    
    @Override
    public void showAd() {
        if (interstitial != null && interstitial.isReady()) {
            interstitial.show();
        }
    }
    
    @Override
    public String getProviderString() {
        return BuildConfig.PROVIDER_NAME;
    }
    
    @Override
    public String getProviderVersionString() {
        return BuildConfig.PROVIDER_VERSION;
    }
    
    @Override
    public void onDestroy() {
        if (interstitial != null) {
            interstitial.destroy();
            interstitial = null;
        }
    }
    
    @Override
    public void onPause() {
        // cannot forward
    }
    
    @Override
    public void onResume() {
        // cannot forward
    }
}
