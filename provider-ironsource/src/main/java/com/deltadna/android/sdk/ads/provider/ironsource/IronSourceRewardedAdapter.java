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

import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

import org.json.JSONObject;

public final class IronSourceRewardedAdapter extends IronSourceAdapter {
    
    private final IronSourceRewardedEventForwarder forwarder;
    
    public IronSourceRewardedAdapter(
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
                waterfallIndex,
                appKey,
                placementName,
                logging);
        
        forwarder = new IronSourceRewardedEventForwarder(this);
        
        IronSource.setRewardedVideoListener(MainThread.redirect(
                forwarder,
                RewardedVideoListener.class));
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        super.requestAd(activity, listener, configuration);
        
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
}
