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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

class DummyInterstitial {
    
    static final int REQUEST_FAIL = 1;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private final Activity activity;
    private final DummyListener listener;
    
    DummyInterstitial(Activity activity, DummyListener listener) {
        this.activity = activity;
        this.listener = listener;
    }
    
    void loadAd(String request) {
        if (request.equals("fail")) {
            listener.onAdFailedToLoad(REQUEST_FAIL);
        } else {
            listener.onAdLoaded();
        }
    }
    
    void show() {
        final AlertDialog alert = new AlertDialog.Builder(activity)
                .setTitle("Dummy Ad")
                .setMessage("You are viewing a dummy ad which will close soon.")
                .setNeutralButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onAdClosed();
                            }})
                .create();
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                alert.show();
                listener.onAdOpened();
            }
        });
        
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        alert.dismiss();
                        listener.onAdClosed();
                    }
                },
                DummyAdapter.DISMISS_AFTER);
    }
}
