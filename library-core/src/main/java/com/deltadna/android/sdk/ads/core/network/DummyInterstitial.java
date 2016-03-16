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

public class DummyInterstitial {

    public static final int BAD_REQUEST = 1;

    private Activity activity;
    private DummyListener listener;
    private int q;
    private int p;

    public DummyInterstitial(Activity activity) {
        this.activity = activity;
    }

    public void setListener(DummyListener listener) {
        this.listener = listener;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public void setP(int p) {
        this.p = p;
    }

    public void loadAd(String request) {

        if (listener == null) {
            return;
        }

        if (request == null) {
            listener.onAdFailed(BAD_REQUEST);
        }
        else {
            listener.onAdReady();
        }
    }


    public void show() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle("Dummy Interstitial")
                        .setMessage("You are viewing a test interstitial ad.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onAdClosed();
                            }
                        })
                        .show();
            }
        });
    }

    public void destroy() {
        listener = null;
    }

}
