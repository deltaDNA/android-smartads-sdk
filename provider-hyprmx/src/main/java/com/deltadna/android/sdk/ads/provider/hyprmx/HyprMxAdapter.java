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

package com.deltadna.android.sdk.ads.provider.hyprmx;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.hyprmx.android.sdk.api.data.OffersAvailableResponse;
import com.hyprmx.android.sdk.utility.OnOffersAvailableResponseListener;

import org.json.JSONObject;

import java.util.Locale;

public final class HyprMxAdapter extends MediationAdapter {
    
    private final String distributorId;
    private final String propertyId;
    private final String userId;
    
    private HyprMXHelper helper;
    
    @Nullable
    private Activity activity;
    @Nullable
    private MediationListener listener;
    @Nullable
    private HyprMXPresentation presentation;
    
    public HyprMxAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String distributorId,
            String propertyId) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.distributorId = distributorId;
        this.propertyId = propertyId;
        this.userId = "deltadna";
    }
    
    @Override
    public void requestAd(
            Activity activity,
            final MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        this.listener = listener;
        
        if (helper == null) {
            helper = HyprMXHelper.getInstance(
                    activity,
                    distributorId,
                    propertyId,
                    userId);
        }
        
        presentation = new HyprMXPresentation();
        presentation.prepare(new OnOffersAvailableResponseListener() {
            @Override
            public void onError(int i, Exception e) {
                Log.w(  BuildConfig.LOG_TAG,
                        String.format(
                                Locale.US,
                                "Presentation error: %d/%s",
                                i,
                                e.getMessage()),
                        e);
                listener.onAdFailedToLoad(
                        HyprMxAdapter.this,
                        AdRequestResult.Error,
                        e.getMessage());
            }
            
            @Override
            public void onOffersAvailable(OffersAvailableResponse response) {
                Log.d(  BuildConfig.LOG_TAG,
                        "Presentation offers available: " + response);
                listener.onAdLoaded(HyprMxAdapter.this);
            }
            
            @Override
            public void onNoOffersAvailable(OffersAvailableResponse response) {
                Log.d(  BuildConfig.LOG_TAG,
                        "Presentation offers not available: " + response);
                listener.onAdFailedToLoad(
                        HyprMxAdapter.this,
                        AdRequestResult.NoFill,
                        "No fill");
            }
        });
    }
    
    @Override
    public void showAd() {
        if (activity != null && listener != null && presentation != null) {
            activity.getApplication().registerActivityLifecycleCallbacks(
                    new Application.ActivityLifecycleCallbacks() {
                        @Override
                        public void onActivityCreated(
                                Activity activity,
                                Bundle savedInstanceState) {
                            
                            if (activity instanceof WrapperActivity) {
                                ((WrapperActivity) activity)
                                        .show(presentation);
                                
                                listener.onAdShowing(HyprMxAdapter.this);
                            }
                        }
                        
                        @Override
                        public void onActivityStarted(Activity activity) {}
                        
                        @Override
                        public void onActivityResumed(Activity activity) {}
                        
                        @Override
                        public void onActivityPaused(Activity activity) {}
                        
                        @Override
                        public void onActivityStopped(Activity activity) {}
                        
                        @Override
                        public void onActivitySaveInstanceState(
                                Activity activity,
                                Bundle outState) {}
                        
                        @Override
                        public void onActivityDestroyed(Activity activity) {
                            if (activity instanceof WrapperActivity) {
                                listener.onAdClosed(
                                        HyprMxAdapter.this,
                                        ((WrapperActivity) activity)
                                                .isComplete());
                                
                                activity.getApplication()
                                        .unregisterActivityLifecycleCallbacks(this);
                            }
                        }
                    });
            
            activity.startActivity(new Intent(activity, WrapperActivity.class));
        }
    }
    
    @Override
    public void onResume() {}
    
    @Override
    public void onPause() {}
    
    @Override
    public void onDestroy() {
        presentation = null;
        activity = null;
        listener = null;
    }
    
    @Override
    public String getProviderString() {
        return BuildConfig.PROVIDER_NAME;
    }
    
    @Override
    public String getProviderVersionString() {
        return BuildConfig.PROVIDER_VERSION;
    }
}
