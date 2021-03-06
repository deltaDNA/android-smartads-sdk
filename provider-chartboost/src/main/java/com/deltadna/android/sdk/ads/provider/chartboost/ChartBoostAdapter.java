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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.chartboost.sdk.CBLocation;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

import org.json.JSONObject;

abstract class ChartBoostAdapter extends MediationAdapter {
    
    public static final String LOCATION = CBLocation.LOCATION_DEFAULT;
    
    private final String appId;
    private final String appSignature;
    protected final String location;
    
    @Nullable
    private Activity activity;
    
    public ChartBoostAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String appId,
            String appSignature,
            String location) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.appId = appId;
        this.appSignature = appSignature;
        this.location = location;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        
        Helper.initialise(
                activity,
                appId,
                appSignature,
                this,
                listener);
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
    public void onResume() {
        if (activity != null) Helper.onResume(activity);
    }
    
    @Override
    public void onPause() {
        if (activity != null) Helper.onPause(activity);
    }
    
    @Override
    public void onDestroy() {
        if (activity != null) Helper.onDestroy(activity);
    }
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
}
