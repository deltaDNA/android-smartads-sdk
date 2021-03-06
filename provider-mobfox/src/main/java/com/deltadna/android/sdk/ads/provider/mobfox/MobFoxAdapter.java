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

package com.deltadna.android.sdk.ads.provider.mobfox;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.mobfox.sdk.interstitial.Interstitial;
import com.mobfox.sdk.networking.MobfoxRequestParams;

import org.json.JSONObject;

public final class MobFoxAdapter extends MediationAdapter {
    
    private static final MobfoxRequestParams PARAMS;
    static {
        PARAMS = new MobfoxRequestParams();
        PARAMS.setParam(MobfoxRequestParams.GDPR, "1");
    }
    
    private final String publicationId;
    
    @Nullable
    private Interstitial ad;
    
    public MobFoxAdapter(
            int eCPM,
            int demoteOnCode,
            Privacy privacy,
            int waterfallIndex,
            String publicationId) {
        
        super(  eCPM,
                demoteOnCode,
                privacy,
                waterfallIndex);
        
        this.publicationId = publicationId;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        PARAMS.setParam(
                MobfoxRequestParams.GDPR_CONSENT,
                privacy.userConsent ? "1" : "0");
        
        ad = new Interstitial(activity, publicationId);
        ad.setListener(new MobFoxEventForwarder(listener, this));
        ad.setRequestParams(PARAMS);
        ad.load();
    }
    
    @Override
    public void showAd() {
        if (ad != null) ad.show();
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
        if (ad != null) ad = null;
    }
    
    @Override
    public void onPause() {}
    
    @Override
    public void onResume() {}
    
    @Override
    public boolean isGdprCompliant() {
        return true;
    }
}
