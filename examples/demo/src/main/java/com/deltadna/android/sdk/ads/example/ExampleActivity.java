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

package com.deltadna.android.sdk.ads.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.ads.DDNASmartAds;
import com.deltadna.android.sdk.ads.core.listeners.AdsListener;
import com.deltadna.android.sdk.ads.core.listeners.RewardedAdsListener;

public class ExampleActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_example);
        
        DDNASmartAds.instance().setAdsListener(new AdsListener() {
            @Override
            public void onRegisteredForAds() {
                Log.d(BuildConfig.LOG_TAG, "Registered for ads");
            }
            
            @Override
            public void onFailedToRegisterForAds(String reason) {
                Log.d(BuildConfig.LOG_TAG, "Failed to register for ads: " + reason);
            }
            
            @Override
            public void onAdOpened() {
                Log.d(BuildConfig.LOG_TAG, "Ad opened");
            }
            
            @Override
            public void onAdFailedToOpen() {
                Log.d(BuildConfig.LOG_TAG, "Ad failed to open");
            }
            
            @Override
            public void onAdClosed() {
                Log.d(BuildConfig.LOG_TAG, "Ad closed");
            }
        });
        
        DDNASmartAds.instance().setRewardedAdsListener(new RewardedAdsListener() {
            @Override
            public void onRegisteredForAds() {
                Log.d(BuildConfig.LOG_TAG, "Registered for rewarded ads");
            }
            
            @Override
            public void onFailedToRegisterForAds(String reason) {
                Log.d(BuildConfig.LOG_TAG, "Failed to register for rewarded ads: " + reason);
            }
            
            @Override
            public void onAdOpened() {
                Log.d(BuildConfig.LOG_TAG, "Ad opened");
            }
            
            @Override
            public void onAdFailedToOpen() {
                Log.d(BuildConfig.LOG_TAG, "Ad failed to open");
            }
            
            @Override
            public void onAdClosed(boolean completed) {
                if (completed) {
                    Log.d(BuildConfig.LOG_TAG, "Rewarded ad closed and completed");
                } else {
                    Log.d(BuildConfig.LOG_TAG, "Rewarded ad closed and not completed");
                }
            }
        });
        
        DDNA.instance().startSdk();
        DDNASmartAds.instance().registerForAds(this);
        
        ((TextView) findViewById(R.id.user_id)).setText(getString(
                R.string.user_id, DDNA.instance().getUserId()));
    }
    
    @Override
    protected void onPause() {
        DDNASmartAds.instance().onPause();
        
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        DDNASmartAds.instance().onResume();
    }
    
    @Override
    protected void onDestroy() {
        DDNASmartAds.instance().onDestroy();
        DDNA.instance().stopSdk();
        
        super.onDestroy();
    }
    
    public void onShowAd(View view) {
        DDNASmartAds.instance().showInterstitialAd();
    }
    
    public void onEngageAd(View view) {
        DDNASmartAds.instance().showInterstitialAd("testAdPoint");
    }
    
    public void onShowRewardedAd(View view) {
        DDNASmartAds.instance().showRewardedAd();
    }
    
    public void onShowEngageRewardedAd(View view) {
        DDNASmartAds.instance().showRewardedAd("testAdPoint");
    }
}
