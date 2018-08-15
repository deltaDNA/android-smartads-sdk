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
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.EngageFactory;
import com.deltadna.android.sdk.ads.Ad;
import com.deltadna.android.sdk.ads.DDNASmartAds;
import com.deltadna.android.sdk.ads.InterstitialAd;
import com.deltadna.android.sdk.ads.RewardedAd;
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExampleActivity extends Activity implements AdRegistrationListener {
    
    private static final String TAG =
            BuildConfig.LOG_TAG
            + ' '
            + ExampleActivity.class.getSimpleName();
    private static final short REFRESH_PERIOD = 500;
    
    private final InterstitialListener interstitialListener =
            new InterstitialListener();
    private final RewardedListener rewardedListener =
            new RewardedListener();
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshStats = new Runnable() {
        @Override
        public void run() {
            updateStats(interstitialAd, interstitialAdStats);
            updateStats(rewardedAd1, rewardedAd1Stats);
            updateStats(rewardedAd2, rewardedAd2Stats);
            
            handler.postDelayed(this, REFRESH_PERIOD);
        }
    };
    
    private TextView interstitialAdMessage;
    private TextView interstitialAdStats;
    private TextView rewardedAd1Message;
    private TextView rewardedAd1Stats;
    private TextView rewardedAd2Message;
    private TextView rewardedAd2Stats;
    
    private Button interstitialAdButton;
    private Button rewardedAd1Button;
    private Button rewardedAd2Button;
    
    private Switch userConsentToggle;
    private Switch ageRestrictedToggle;
    
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd1;
    private RewardedAd rewardedAd2;
    
    // lifecycle methods
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_example);
        
        interstitialAdMessage = findViewById(R.id.interstitial_ad_message);
        interstitialAdStats = findViewById(R.id.interstitial_ad_stats);
        rewardedAd1Message = findViewById(R.id.rewarded_ad1_message);
        rewardedAd1Stats = findViewById(R.id.rewarded_ad1_stats);
        rewardedAd2Message = findViewById(R.id.rewarded_ad2_message);
        rewardedAd2Stats = findViewById(R.id.rewarded_ad2_stats);
        
        interstitialAdButton = findViewById(R.id.interstitial_ad);
        rewardedAd1Button = findViewById(R.id.rewarded_ad1);
        rewardedAd2Button = findViewById(R.id.rewarded_ad2);
        
        userConsentToggle = findViewById(R.id.user_consent);
        ageRestrictedToggle = findViewById(R.id.age_restricted);
        
        DDNASmartAds.instance().setAdRegistrationListener(this);
        
        DDNA.instance().startSdk();
        
        ((TextView) findViewById(R.id.user_id)).setText(getString(
                R.string.user_id, DDNA.instance().getUserId()));
        handler.postDelayed(refreshStats, REFRESH_PERIOD);
    }
    
    @Override
    protected void onDestroy() {
        handler.removeCallbacks(refreshStats);
        DDNA.instance().stopSdk();
        
        super.onDestroy();
    }
    
    // view callbacks
    
    public void interstitialAd(View view) {
        /*
         * Don't worry about checking if an ad is ready. Trying to show an ad
         * when you want will give a report of your fill rate.
         */
        if (interstitialAd != null) interstitialAd.show();
    }
    
    public void rewardedAd1(View view) {
        if (rewardedAd1 != null && rewardedAd1.isReady()) {
            rewardedAd1.show();
        }
    }
    
    public void rewardedAd2(View view) {
        if (rewardedAd2 != null && rewardedAd2.isReady()) {
            rewardedAd2.show();
        }
    }
    
    public void onNewSession(View view) {
        interstitialAdButton.setEnabled(false);
        interstitialAdMessage.setText("");
        interstitialAdStats.setText("");
        rewardedAd1Button.setEnabled(false);
        rewardedAd1Message.setText("");
        rewardedAd1Stats.setText("");
        rewardedAd2Button.setEnabled(false);
        rewardedAd2Message.setText("");
        rewardedAd2Stats.setText("");
        
        DDNA.instance().newSession();
    }
    
    public void onForgetMe(View view) {
        DDNA.instance().forgetMe();
    }
    
    public void onUserConsent(View view) {
        DDNASmartAds.instance().getSettings()
                .setUserConsent(userConsentToggle.isChecked());
        DDNA.instance().newSession();
    }
    
    public void onAgeRestricted(View view) {
        DDNASmartAds.instance().getSettings()
                .setAgeRestrictedUser(ageRestrictedToggle.isChecked());
        DDNA.instance().newSession();
    }
    
    private void updateStats(Ad action, TextView view) {
        if (action == null) return;
        
        final Date lastShown = action.getLastShown();
        final String lastShownText = (lastShown == null)
                ? "N/A"
                : DateFormat.getTimeFormat(this).format(lastShown);
        final int adShowWaitSecs = (lastShown == null)
                ? 0
                : (int) TimeUnit.MILLISECONDS.toSeconds(lastShown.getTime()
                - new Date().getTime())
                + action.getAdShowWaitSecs();
        final String secsText = (adShowWaitSecs == 0)
                ? ""
                : " (" + Math.max(0, adShowWaitSecs) + " secs)";
        
        view.setText(getString(
                R.string.stats,
                action.getSessionCount(),
                action.getSessionLimit(),
                action.getDailyCount(),
                action.getDailyLimit(),
                lastShownText,
                secsText));
    }
    
    // ad registration callbacks
    
    @Override
    public void onRegisteredForInterstitial() {
        Log.i(TAG, "Registered for interstitial ads");
        
        interstitialAdMessage.setText(null);
        
        DDNASmartAds.instance().getEngageFactory().requestInterstitialAd(
                "interstitialAd",
                new EngageFactory.Callback<InterstitialAd>() {
                    @Override
                    public void onCompleted(InterstitialAd action) {
                        interstitialAd = action.setListener(interstitialListener);
                        
                        interstitialAdButton.setEnabled(true);
                    }
                });
    }
    
    @Override
    public void onFailedToRegisterForInterstitial(String reason) {
        Log.i(TAG, "Failed to register for interstitial ads");
        
        interstitialAdMessage.setText(R.string.failed_to_register_interstitial);
        interstitialAdButton.setEnabled(false);
        
        interstitialAd = null;
    }
    
    @Override
    public void onRegisteredForRewarded() {
        Log.i(TAG, "Registered for rewarded ads");
        
        rewardedAd1Message.setText(null);
        rewardedAd2Message.setText(null);
        
        DDNASmartAds.instance().getEngageFactory().requestRewardedAd(
                "rewardedAd1",
                new EngageFactory.Callback<RewardedAd>() {
                    @Override
                    public void onCompleted(RewardedAd action) {
                        rewardedAd1 = action.setListener(rewardedListener);
                        
                        rewardedAd1Button.setEnabled(true);
                    }
                });
        DDNASmartAds.instance().getEngageFactory().requestRewardedAd(
                "rewardedAd2",
                new EngageFactory.Callback<RewardedAd>() {
                    @Override
                    public void onCompleted(RewardedAd action) {
                        rewardedAd2 = action.setListener(rewardedListener);
                        
                        rewardedAd2Button.setEnabled(true);
                    }
                });
    }
    
    @Override
    public void onFailedToRegisterForRewarded(String reason) {
        Log.d(TAG, "Failed to register for rewarded ads");
        
        rewardedAd1Message.setText(R.string.failed_to_register_rewarded);
        rewardedAd2Message.setText(R.string.failed_to_register_rewarded);
        rewardedAd1Button.setEnabled(false);
        rewardedAd2Button.setEnabled(false);
        
        rewardedAd1 = null;
        rewardedAd2 = null;
    }
    
    private final class InterstitialListener implements InterstitialAdsListener {
        
        @Override
        public void onOpened(InterstitialAd ad) {
            Log.i(BuildConfig.LOG_TAG, "On opened " + ad);
            interstitialAdMessage.setText(R.string.fulfilled);
        }
        
        @Override
        public void onFailedToOpen(InterstitialAd ad, String reason) {
            Log.i(  BuildConfig.LOG_TAG,
                    "On failed to open " + ad + " due to " + reason);
            interstitialAdMessage.setText(getString(R.string.failed_to_open, reason));
        }
        
        @Override
        public void onClosed(InterstitialAd ad) {
            Log.i(BuildConfig.LOG_TAG, "On closed " + ad);
        }
    }
    
    private final class RewardedListener implements RewardedAdsListener {
        
        @Override
        public void onLoaded(RewardedAd ad) {
            Log.i(BuildConfig.LOG_TAG, "On loaded " + ad);
            
            if (ad == rewardedAd1) {
                rewardedAd1Button.setEnabled(true);
                rewardedAd1Message.setText(R.string.ready);
            } else if (ad == rewardedAd2) {
                rewardedAd2Button.setEnabled(true);
                rewardedAd2Message.setText(R.string.ready);
            }
        }
        
        @Override
        public void onExpired(RewardedAd ad) {
            Log.i(BuildConfig.LOG_TAG, "On expired " + ad);
            
            if (ad == rewardedAd1) {
                rewardedAd1Button.setEnabled(false);
                rewardedAd1Message.setText(R.string.expired);
            } else if (ad == rewardedAd2) {
                rewardedAd2Button.setEnabled(false);
                rewardedAd2Message.setText(R.string.expired);
            }
        }
        
        @Override
        public void onOpened(RewardedAd ad) {
            Log.i(BuildConfig.LOG_TAG, "On opened " + ad);
            
            if (ad == rewardedAd1) {
                rewardedAd1Button.setEnabled(false);
                rewardedAd1Message.setText(R.string.fulfilled);
            } else if (ad == rewardedAd2) {
                rewardedAd2Button.setEnabled(false);
                rewardedAd2Message.setText(R.string.fulfilled);
            }
        }
        
        @Override
        public void onFailedToOpen(RewardedAd ad, String reason) {
            Log.i(  BuildConfig.LOG_TAG,
                    "On failed to open " + ad + " due to " + reason);
            
            if (ad == rewardedAd1) {
                rewardedAd1Button.setEnabled(false);
                rewardedAd1Message.setText(getString(R.string.failed_to_open, reason));
            } else if (ad == rewardedAd2) {
                rewardedAd2Button.setEnabled(false);
                rewardedAd2Message.setText(getString(R.string.failed_to_open, reason));
            }
        }
        
        @Override
        public void onClosed(RewardedAd ad, boolean completed) {
            Log.i(BuildConfig.LOG_TAG, "On closed " + ad + " with " + completed);
            
            final String message = completed
                    ? getString(
                            R.string.watched,
                            ad.getRewardAmount(),
                            ad.getRewardType())
                    : getString(R.string.skipped);
            if (ad == rewardedAd1) {
                rewardedAd1Button.setEnabled(false);
                rewardedAd1Message.setText(message);
            } else if (ad == rewardedAd2) {
                rewardedAd2Button.setEnabled(false);
                rewardedAd2Message.setText(message);
            }
        }
    }
}
