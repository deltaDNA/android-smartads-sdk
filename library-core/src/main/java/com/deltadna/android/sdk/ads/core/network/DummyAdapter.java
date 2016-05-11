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

package com.deltadna.android.sdk.ads.core.network;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

public final class DummyAdapter extends MediationAdapter {
    
    public static final int DISMISS_AFTER = 1000;
    
    @Nullable
    private DummyInterstitial interstitial;
    
    public DummyAdapter(int eCPM, int demoteOnCode, int waterfallIndex) {
        super(eCPM, demoteOnCode, waterfallIndex);
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject mediationParams) {
        
        interstitial = new DummyInterstitial(
                activity,
                new DummyEventForwarder(listener, this));
        interstitial.loadAd("request");
    }
    
    @Override
    public void showAd() {
        if (interstitial != null) {
            interstitial.show();
        }
    }
    
    @Override
    public String getProviderString() {
        return "Dummy";
    }
    
    @Override
    public String getProviderVersionString() {
        return "1.0";
    }
    
    @Override
    public void onDestroy() {
        interstitial = null;
    }
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
}
