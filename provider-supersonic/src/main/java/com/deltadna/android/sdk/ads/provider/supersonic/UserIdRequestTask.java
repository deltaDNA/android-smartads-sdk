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
import android.os.Handler;
import android.os.Looper;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

abstract class UserIdRequestTask extends AsyncTask<Void, Void, String> {
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    protected final Activity activity;
    protected final MediationAdapter adapter;
    protected final MediationListener listener;
    
    UserIdRequestTask(
            Activity activity,
            MediationAdapter adapter,
            MediationListener listener) {
        
        this.activity = activity;
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    protected String doInBackground(Void... params) {
        try {
            // TODO should we cache the value?
            return AdvertisingIdClient.getAdvertisingIdInfo(activity).getId();
        } catch (final IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onAdFailedToLoad(
                            adapter,
                            AdRequestResult.Network,
                            e.getMessage());
                }
            });
        } catch (final GooglePlayServicesNotAvailableException
                | GooglePlayServicesRepairableException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onAdFailedToLoad(
                            adapter,
                            AdRequestResult.Error,
                            e.getMessage());
                }
            });
        }
        
        return null;
    }
}
