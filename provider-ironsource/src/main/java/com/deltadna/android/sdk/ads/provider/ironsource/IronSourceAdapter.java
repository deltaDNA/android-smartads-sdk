/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
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

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.LogListener;

import org.json.JSONObject;

import java.util.Locale;

abstract class IronSourceAdapter extends MediationAdapter {
    
    private final String appKey;
    @Nullable
    final String placementName;
    
    @Nullable
    private Activity activity;
    
    IronSourceAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appKey,
            @Nullable String placementName,
            boolean logging) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appKey = appKey;
        this.placementName = placementName;
        
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
        
        Helper.initialise(activity, privacy, appKey);
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
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
}
