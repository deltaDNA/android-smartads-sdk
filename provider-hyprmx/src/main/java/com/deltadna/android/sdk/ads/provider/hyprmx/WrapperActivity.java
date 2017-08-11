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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hyprmx.android.sdk.HyprMXHelper;
import com.hyprmx.android.sdk.HyprMXPresentation;
import com.hyprmx.android.sdk.api.data.Offer;

public final class WrapperActivity extends Activity {
    
    private boolean complete;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        HyprMXHelper.processActivityResult(
                this,
                requestCode,
                resultCode,
                data,
                new HyprMXHelper.HyprMXListener() {
                    @Override
                    public void onOfferCompleted(Offer offer) {
                        Log.d(BuildConfig.LOG_TAG, "Offer completed: " + offer);
                        complete = true;
                    }
                    
                    @Override
                    public void onOfferCancelled(Offer offer) {
                        Log.d(BuildConfig.LOG_TAG, "Offer cancelled: " + offer);
                        complete = false;
                    }
                    
                    @Override
                    public void onNoContentAvailable() {
                        Log.d(BuildConfig.LOG_TAG, "Ni content available");
                    }
                    
                    @Override
                    public void onUserOptedOut() {
                        Log.d(BuildConfig.LOG_TAG, "User opted out");
                        complete = false;
                    }
        });
        finish();
    }
    
    void show(HyprMXPresentation presentation) {
        presentation.show(this);
    }
    
    boolean isComplete() {
        return complete;
    }
}
