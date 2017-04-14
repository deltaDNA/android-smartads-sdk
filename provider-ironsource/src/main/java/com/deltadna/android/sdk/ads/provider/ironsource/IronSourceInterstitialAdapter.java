/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.ironsource;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import org.json.JSONObject;

import java.util.Locale;

public final class IronSourceInterstitialAdapter extends MediationAdapter {
    
    private final String appKey;
    
    @Nullable
    private Activity activity;
    
    private static boolean initialised;
    
    public IronSourceInterstitialAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appKey,
            boolean logging) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appKey = appKey;
        
        if (logging) {
            IronSource.setLogListener(new LogListener() {
                @Override
                public void onLog(
                        IronSourceLogger.IronSourceTag tag,
                        String s,
                        int i) {
                    
                    Log.d(BuildConfig.LOG_TAG, String.format(
                            Locale.US,
                            "%s, %s, %d",
                            tag,
                            s,
                            i));
                }
            });
        }
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        
        synchronized (IronSourceInterstitialAdapter.class) {
            if (!initialised) {
                IronSource.init(activity, appKey, IronSource.AD_UNIT.INTERSTITIAL);
                initialised = true;
            }
        }
        
        IronSource.setInterstitialListener(MainThread.redirect(
                new IronSourceInterstitialEventForwarder(this, listener),
                InterstitialListener.class));
        IronSource.loadInterstitial();
    }
    
    @Override
    public void showAd() {
        if (IronSource.isInterstitialReady()) {
            IronSource.showInterstitial();
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
        if (activity != null) activity = null;
    }
    
    @Override
    public void onPause() {
        if (activity != null) IronSource.onPause(activity);
    }
    
    @Override
    public void onResume() {
        if (activity != null) IronSource.onResume(activity);
    }
}
