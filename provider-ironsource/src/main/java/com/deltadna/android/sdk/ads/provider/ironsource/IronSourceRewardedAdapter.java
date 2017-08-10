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
import android.text.TextUtils;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import org.json.JSONObject;

import java.util.Locale;

public final class IronSourceRewardedAdapter extends MediationAdapter {
    
    private final String appKey;
    @Nullable
    private final String placementName;
    
    private final IronSourceRewardedEventForwarder forwarder;
    
    @Nullable
    private Activity activity;
    
    public IronSourceRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appKey,
            @Nullable String placementName,
            boolean logging) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appKey = appKey;
        this.placementName = placementName;
        
        forwarder = new IronSourceRewardedEventForwarder(this);
        
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
        
        IronSource.setRewardedVideoListener(MainThread.redirect(
                forwarder,
                RewardedVideoListener.class));
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        
        Helper.initialise(activity, appKey);
        
        forwarder.requestPerformed(listener);
    }
    
    @Override
    public void showAd() {
        if (IronSource.isRewardedVideoAvailable()) {
            if (TextUtils.isEmpty(placementName)) {
                IronSource.showRewardedVideo();
            } else {
                IronSource.showRewardedVideo(placementName);
            }
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
