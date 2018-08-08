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

package com.deltadna.android.sdk.ads.provider.vungle;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.error.VungleException;

final class LoadEventForwarder extends EventForwarder implements LoadAdCallback {
    
    LoadEventForwarder(
            String placementId,
            MediationAdapter adapter,
            MediationListener listener) {
        
        super(placementId, adapter, listener);
    }
    
    @Override
    public void onError(String placementId, Throwable throwable) {
        Log.w(  BuildConfig.LOG_TAG,
                "On ad load error: " + throwable.getLocalizedMessage(),
                throwable);
        
        if (!isSamePlacement(placementId)) return;
        
        final AdRequestResult result;
        if (throwable instanceof VungleException) {
            final VungleException e = (VungleException) throwable;
            switch (e.getExceptionCode()) {
                case VungleException.NO_SERVE:
                    result = AdRequestResult.NoFill;
                    break;
                
                case VungleException.ZERO_PLACEMENTS:
                case VungleException.CONFIGURATION_ERROR:
                case VungleException.NO_AUTO_CACHED_PLACEMENT:
                case VungleException.PLACEMENT_NOT_FOUND:
                    result = AdRequestResult.Configuration;
                    break;
                
                case VungleException.AD_FAILED_TO_DOWNLOAD:
                    result = AdRequestResult.Network;
                    break;
                
                default:
                    result = AdRequestResult.Error;
            }
        } else {
            result = AdRequestResult.Error;
        }
        
        listener.onAdFailedToLoad(
                adapter,
                result,
                throwable.getLocalizedMessage());
    }
    
    @Override
    public void onAdLoad(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "On ad load: " + placementId);
        
        if (!isSamePlacement(placementId)) return;
        
        listener.onAdLoaded(adapter);
    }
}
