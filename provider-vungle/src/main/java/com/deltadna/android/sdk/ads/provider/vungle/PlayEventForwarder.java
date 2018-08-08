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

import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.error.VungleException;

import java.util.Locale;

final class PlayEventForwarder extends EventForwarder implements PlayAdCallback {
    
    PlayEventForwarder(
            String placementId,
            MediationAdapter adapter,
            MediationListener listener) {
        
        super(placementId, adapter, listener);
    }
    
    @Override
    public void onError(String placementId, Throwable throwable) {
        Log.w(  BuildConfig.LOG_TAG,
                "On ad show error: " + throwable.getLocalizedMessage(),
                throwable);
        
        if (!isSamePlacement(placementId)) return;
        
        final AdShowResult result;
        if (throwable instanceof VungleException) {
            final VungleException e = (VungleException) throwable;
            switch (e.getExceptionCode()) {
                case VungleException.AD_EXPIRED:
                    result = AdShowResult.EXPIRED;
                    break;
                
                default:
                    result = AdShowResult.ERROR;
            }
        } else {
            result = AdShowResult.ERROR;
        }
        
        listener.onAdFailedToShow(adapter, result);
    }
    
    @Override
    public void onAdStart(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "On ad start: " + placementId);
        
        if (!isSamePlacement(placementId)) return;
        
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdEnd(String placementId, boolean completed, boolean clicked) {
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.ENGLISH,
                "On ad end: %s/%s/%s",
                placementId,
                completed,
                clicked));
        
        if (!isSamePlacement(placementId)) return;
        
        if (clicked) listener.onAdClicked(adapter);
        listener.onAdClosed(adapter, completed);
    }
}
