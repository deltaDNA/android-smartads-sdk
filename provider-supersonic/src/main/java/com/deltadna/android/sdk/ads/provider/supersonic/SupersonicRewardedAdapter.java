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

package com.deltadna.android.sdk.ads.provider.supersonic;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.supersonic.mediationsdk.sdk.Supersonic;
import com.supersonic.mediationsdk.sdk.SupersonicFactory;

import org.json.JSONObject;

public final class SupersonicRewardedAdapter extends MediationAdapter {
    
    private static final Supersonic AGENT = SupersonicFactory.getInstance();
    
    private final String appKey;
    
    @Nullable
    private Activity activity;
    @Nullable
    private AsyncTask requestTask;
    
    public SupersonicRewardedAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String appKey,
            boolean log) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.appKey = appKey;
        
        if (log) AGENT.setLogListener(new LogDelegate());
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        this.activity = activity;
        
        if (requestTask != null && !requestTask.isCancelled()) {
            requestTask.cancel(false);
        }
        requestTask = new RequestTask(activity, this, listener).execute();
    }
    
    @Override
    public void showAd() {
        AGENT.showRewardedVideo();
    }
    
    @Override
    public String getProviderString() {
        return "SUPERSONIC";
    }
    
    @Override
    public String getProviderVersionString() {
        return Version.VALUE;
    }
    
    @Override
    public void onDestroy() {
        AGENT.setLogListener(null);
        if (requestTask != null && !requestTask.isCancelled()) {
            requestTask.cancel(true);
            requestTask = null;
        }
        
        activity = null;
    }
    
    @Override
    public void onPause() {
        if (activity != null) AGENT.onPause(activity);
    }
    
    @Override
    public void onResume() {
        if (activity != null) AGENT.onResume(activity);
    }
    
    private final class RequestTask extends UserIdRequestTask {
        
        RequestTask(
                Activity activity,
                MediationAdapter adapter,
                MediationListener listener) {
            
            super(activity, adapter, listener);
        }
        
        @Override
        protected void onPostExecute(String userId) {
            super.onPostExecute(userId);
            
            if (userId != null) {
                AGENT.setRewardedVideoListener(
                        new SupersonicRewardedForwarder(
                                SupersonicRewardedAdapter.this,
                                listener));
                AGENT.initRewardedVideo(activity, appKey, userId);
            }
        }
    }
}
