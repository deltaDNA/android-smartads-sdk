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

package com.deltadna.android.sdk.ads.provider.inmobi;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONObject;

abstract class InMobiAdapter<T extends EventForwarder> extends MediationAdapter {
    
    private static boolean initialised;
    
    private final String accountId;
    private final Long placementId;
    private final boolean logging;
    
    @Nullable
    private InMobiInterstitial ad;
    @Nullable
    private T forwarder;
    
    InMobiAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String accountId,
            Long placementId,
            boolean logging) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.accountId = accountId;
        this.placementId = placementId;
        this.logging = logging;
    }
    
    protected abstract T createListener(MediationListener listener);
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject mediationParams) {
        
        synchronized (InMobiAdapter.class) {
            if (!initialised) {
                Log.d(BuildConfig.LOG_TAG, "Initialising SDK");
                
                if (logging) InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG);
                InMobiSdk.init(activity, accountId);
                
                initialised = true;
            }
        }
        
        forwarder = createListener(listener);
        ad = new InMobiInterstitial(
                activity,
                placementId,
                forwarder);
        ad.load();
    }
    
    @Override
    public void showAd() {
        if (ad != null && ad.isReady()) {
            ad.show();
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
        ad = null;
        forwarder = null;
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
