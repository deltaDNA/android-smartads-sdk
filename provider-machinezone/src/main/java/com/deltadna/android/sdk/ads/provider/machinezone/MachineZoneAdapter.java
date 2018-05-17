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

package com.deltadna.android.sdk.ads.provider.machinezone;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.fractionalmedia.sdk.AdRequest;
import com.fractionalmedia.sdk.AdZone;
import com.fractionalmedia.sdk.AdZoneError;
import com.fractionalmedia.sdk.InitializationListener;
import com.fractionalmedia.sdk.InterstitialActivity;

import org.json.JSONObject;

import java.util.HashMap;

abstract class MachineZoneAdapter extends MediationAdapter {
    
    protected final String adUnitId;
    
    private boolean initialised;
    @Nullable
    private Application application;
    @Nullable
    private MediationListener listener;
    @Nullable
    private AdRequest request;
    
    MachineZoneAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String adUnitId) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.adUnitId = adUnitId;
    }
    
    @Override
    public final void requestAd(
            final Activity activity,
            final MediationListener l,
            JSONObject configuration) {
        
        if (!initialised) {
            Log.d(BuildConfig.LOG_TAG, "Initialising SDK");
            
            AdZone.Start(
                    activity.getApplication(),
                    new InitializationListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(BuildConfig.LOG_TAG, "Initialised SDK");
                            initialised = true;
                            
                            application = activity.getApplication();
                            listener = l;
                            request = null;
                            requestAd(activity.getApplicationContext(), l);
                        }
                        
                        @Override
                        public void onFailure(AdZoneError error) {
                            Log.w(  BuildConfig.LOG_TAG,
                                    "Failed to initialise SDK: " + error);
                            initialised = false;
                            
                            l.onAdFailedToLoad(
                                    MachineZoneAdapter.this,
                                    AdRequestResult.Configuration,
                                    error.toString());
                        }
                    },
                    new HashMap<String, Object>());
        } else {
            application = activity.getApplication();
            listener = l;
            request = null;
            requestAd(activity.getApplicationContext(), l);
        }
    }
    
    protected abstract void requestAd(
            Context context,
            MediationListener listener);
    
    final void onAdLoaded(AdRequest request) {
        this.request = request;
    }
    
    @Override
    public final void showAd() {
        if (    application != null && listener != null &&
                request != null && request.isReadyToShow()) {
            application.registerActivityLifecycleCallbacks(
                    new Application.ActivityLifecycleCallbacks() {
                        @Override
                        public void onActivityCreated(
                                Activity activity,
                                Bundle bundle) {}
                        
                        @Override
                        public void onActivityStarted(Activity activity) {
                            if (activity instanceof InterstitialActivity) {
                                Log.d(  BuildConfig.LOG_TAG,
                                        "On activity created: " + activity);
                                listener.onAdShowing(MachineZoneAdapter.this);
                            }
                            
                            application.unregisterActivityLifecycleCallbacks(this);
                        }
                        
                        @Override
                        public void onActivityResumed(Activity activity) {}
                        
                        @Override
                        public void onActivityPaused(Activity activity) {}
                        
                        @Override
                        public void onActivityStopped(Activity activity) {}
                        
                        @Override
                        public void onActivitySaveInstanceState(
                                Activity activity,
                                Bundle bundle) {}
                                
                        @Override
                        public void onActivityDestroyed(Activity activity) {}
                    });
            
            request.Show();
            request = null;
        }
    }
    
    @Override
    public final String getProviderString() {
        return BuildConfig.PROVIDER_NAME;
    }
    
    @Override
    public final String getProviderVersionString() {
        return BuildConfig.PROVIDER_VERSION;
    }
    
    @Override
    public final void onResume() { /* cannot forward */ }
    
    @Override
    public final void onPause() { /* cannot forward */ }
    
    @Override
    public final void onDestroy() { /* cannot forward */ }
}
