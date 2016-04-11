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

package com.deltadna.android.sdk.ads.provider.mobfox;

import android.app.Activity;
import android.util.Log;

import com.adsdk.sdk.AdManager;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;

import org.json.JSONObject;

public final class MobFoxAdapter extends MediationAdapter {
    
    private final String publicationId;
    
    private AdManager adManager;
    
    public MobFoxAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String publicationId) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.publicationId = publicationId;
    }
    
    @Override
    public void requestAd(Activity activity, MediationListener listener, JSONObject configuration) {
        try {
            if (adManager == null) {
                adManager = new AdManager(activity, "http://my.mobfox.com/request.php", publicationId, true);
                adManager.setInterstitialAdsEnabled(true);
                adManager.setVideoAdsEnabled(true);
            }
        } catch (Exception e) {
            Log.e(BuildConfig.LOG_TAG, "Failed to initialise", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Configuration,
                    "Invalid Configuration: " + e);
        }
        
        try {
            adManager.setListener(new MobFoxEventForwarder(listener, this));
            adManager.requestAd();
        } catch (Exception e) {
            Log.e(BuildConfig.LOG_TAG, "Failed to request ad", e);
            listener.onAdFailedToLoad(
                    this,
                    AdRequestResult.Error,
                    "Failed to request MobFox ad: " + e);
        }
    }

    @Override
    public void showAd() {
        if(adManager != null && adManager.isAdLoaded()) {
            adManager.showAd();
        }
    }

    @Override
    public String getProviderString() {
        return "MobFox";
    }

    @Override
    public String getProviderVersionString() {
        return "7.0.8";
    }
    
    @Override
    public void onDestroy() {
        if (adManager != null) {
            adManager.release();
            adManager = null;
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
