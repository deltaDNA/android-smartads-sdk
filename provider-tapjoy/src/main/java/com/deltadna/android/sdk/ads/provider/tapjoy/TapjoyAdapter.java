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

package com.deltadna.android.sdk.ads.provider.tapjoy;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJPlacement;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;

import org.json.JSONObject;

import java.util.Hashtable;

/**
 * Singular implementation handles both interstitial and rewarded ads.
 */
public final class TapjoyAdapter extends MediationAdapter {
    
    private static final Handler REQUESTER = new Handler(Looper.getMainLooper());
    
    private static boolean initialising;
    private static boolean initialised;
    
    private final String sdkKey;
    private final String placementName;
    private final boolean logging;
    
    @Nullable
    private TJPlacement ad;
    @Nullable
    private MediationListener listener;
    
    public TapjoyAdapter(
            int eCPM,
            int demoteOnCode,
            int waterfallIndex,
            String sdkKey,
            String placementName,
            boolean logging) {
        
        super(eCPM, demoteOnCode, waterfallIndex);
        
        this.sdkKey = sdkKey;
        this.placementName = placementName;
        this.logging = logging;
    }
    
    @Override
    public void requestAd(
            Activity activity,
            MediationListener listener,
            JSONObject configuration) {
        
        synchronized (TapjoyAdapter.class) {
            final Request request = new Request(activity, listener);
            
            if (!initialising && !initialised) {
                initialising = true;
                
                final Hashtable<String, Object> flags = new Hashtable<>(1);
                flags.put(TapjoyConnectFlag.ENABLE_LOGGING, Boolean.toString(logging));
                
                Tapjoy.connect(
                        activity.getApplicationContext(),
                        sdkKey,
                        flags,
                        new ConnectionListener(request));
            }
            
            if (!initialising) {
                REQUESTER.removeCallbacks(request);
                REQUESTER.post(request);
            }
        }
    }
    
    @Override
    public void showAd() {
        if (ad != null) {
            if (ad.isContentReady()) {
                ad.showContent();
                
                ad = null;
                listener = null;
            } else if (listener != null) {
                Log.w(BuildConfig.LOG_TAG, "Ad is not ready");
                listener.onAdFailedToShow(this, AdClosedResult.NOT_READY);
            } else {
                Log.w(BuildConfig.LOG_TAG, "Listener is null");
            }
        } else {
            Log.w(BuildConfig.LOG_TAG, "Ad is null");
        }
    }
    
    @Override
    public void onResume() {}
    
    @Override
    public void onPause() {}
    
    @Override
    public void onDestroy() {
        Tapjoy.setActivity(null);
        ad = null;
        listener = null;
    }
    
    @Override
    public String getProviderString() {
        return BuildConfig.PROVIDER_NAME;
    }
    
    @Override
    public String getProviderVersionString() {
        return BuildConfig.PROVIDER_VERSION;
    }
    
    private class Request implements Runnable {
        
        private final Activity activity;
        private final MediationListener listener;
        
        Request(Activity activity, MediationListener listener) {
            this.activity = activity;
            this.listener = listener;
        }
        
        @Override
        public void run() {
            if (initialised) {
                Tapjoy.setActivity(activity);
                TapjoyAdapter.this.listener = listener;
                
                ad = Tapjoy.getPlacement(placementName, new EventForwarder(
                        TapjoyAdapter.this,
                        listener,
                        placementName));
                ad.requestContent();
            } else {
                listener.onAdFailedToLoad(
                        TapjoyAdapter.this,
                        AdRequestResult.Configuration,
                        "SDK not initialised");
            }
        }
    }
    
    private class ConnectionListener implements TJConnectListener {
        
        private final Request request;
        
        ConnectionListener(Request request) {
            this.request = request;
        }
        
        @Override
        public void onConnectSuccess() {
            Log.d(BuildConfig.LOG_TAG, "Connected");
            
            synchronized (TapjoyAdapter.class) {
                REQUESTER.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(BuildConfig.LOG_TAG, "Requesting first placement");
                        Tapjoy.setActivity(request.activity);
                        Tapjoy.getPlacement(placementName, EventForwarder.EMPTY);
                    }
                });
                
                REQUESTER.post(request);
                
                initialised = true;
                initialising = false;
            }
        }
        
        @Override
        public void onConnectFailure() {
            Log.d(BuildConfig.LOG_TAG, "Failed to connect");
            
            synchronized (TapjoyAdapter.class) {
                initialised = false;
                initialising = false;
            }
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + '@' + placementName;
    }
}
