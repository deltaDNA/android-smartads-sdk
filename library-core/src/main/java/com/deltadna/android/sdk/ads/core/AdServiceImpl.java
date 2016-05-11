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

package com.deltadna.android.sdk.ads.core;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class AdServiceImpl implements AdService {
    
    public static final String AD_TYPE_UNKNOWN = "UNKNOWN";
    public static final String AD_TYPE_INTERSTITIAL = "INTERSTITIAL";
    public static final String AD_TYPE_REWARDED = "REWARDED";
    
    private static final int TIME_ONE_SECOND = 60 * 1000;
    private static final String VERSION = "SmartAds v" + BuildConfig.VERSION_NAME;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private final Activity activity;
    private final AdServiceListener listener;
    
    private final ConnectivityManager connectivity;
    private final AdAgentListener agentListener;
    
    private String decisionPoint;
    
    private JSONObject adConfiguration;
    
    private AdAgent interstitialAgent;
    private AdAgent rewardedAgent;
    
    private int sessionAdCount = 0;
    private long lastAdShownTime = 0;
    
    private int adMinimumInterval;
    private int adMaxPerSession;
    private boolean adDebugMode = true;
    
    AdServiceImpl(Activity activity, AdServiceListener listener) {
        Preconditions.checkArg(activity != null, "activity cannot be null");
        Preconditions.checkArg(listener != null, "listener cannot be null");
        
        this.activity = activity;
        this.listener = MainThread.redirect(listener, AdServiceListener.class);
        
        connectivity = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        agentListener = new AgentListener();
    }
    
    @Override
    public void init(String decisionPoint) {
        Log.d(  BuildConfig.LOG_TAG,
                "Initialising AdService version " + VERSION);
        
        this.decisionPoint = decisionPoint;
        
        requestAdConfiguration();
    }
    
    @Override
    public boolean isInterstitialAdAvailable() {
        return interstitialAgent != null && interstitialAgent.isAdLoaded();
    }
    
    @Override
    public boolean isRewardedAdAvailable() {
        return rewardedAgent != null && rewardedAgent.isAdLoaded();
    }
    
    @Override
    public void showInterstitialAd(@Nullable String adPoint) {
        if (interstitialAgent != null) {
            showAd(interstitialAgent, adPoint);
        } else {
            listener.onInterstitialAdFailedToOpen(
                    "Interstitial agent is not initialised");
        }
    }
    
    @Override
    public void showRewardedAd(@Nullable String adPoint) {
        if (rewardedAgent != null) {
            showAd(rewardedAgent, adPoint);
        } else {
            listener.onRewardedAdFailedToOpen(
                    "Rewarded agent is not initialised");
        }
    }
    
    @Override
    public void onPause() {
        if (interstitialAgent != null) {
            interstitialAgent.onPause();
        }
        if (rewardedAgent != null) {
            rewardedAgent.onPause();
        }
    }
    
    @Override
    public void onResume() {
        if (interstitialAgent != null) {
            interstitialAgent.onResume();
        }
        if (rewardedAgent != null) {
            rewardedAgent.onResume();
        }
    }
    
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        
        if (interstitialAgent != null) {
            interstitialAgent.onDestroy();
        }
        if (rewardedAgent != null) {
            rewardedAgent.onDestroy();
        }
    }
    
    private void showAd(final AdAgent agent, @Nullable final String adPoint) {
        MediationAdapter mediationAdapter;
        if (agent != null) {
            mediationAdapter = agent.getCurrentAdapter();
            if (adPoint != null) {
                agent.setAdPoint(adPoint);
            }
            
            if (!agent.isAdLoaded()) {
                postAdShowEvent(
                        agent,
                        mediationAdapter,
                        AdShowResult.NOT_REGISTERED);
                if (agent.equals(interstitialAgent)) {
                    listener.onInterstitialAdFailedToOpen(
                            "Interstitial ad agent not registered");
                } else if (agent.equals(rewardedAgent)) {
                    listener.onRewardedAdFailedToOpen(
                            "Rewarded ad agent not registered");
                }
                return;
            }
        } else {
            postAdShowEvent(
                    null,
                    null,
                    AdShowResult.NOT_READY);
            // FIXME should notify listener here as well, but don't know which
            return;
        }
        
        if (    System.currentTimeMillis() - lastAdShownTime
                <= adMinimumInterval * 1000) {
            Log.w(BuildConfig.LOG_TAG, "Not showing ad before minimum interval");
            
            postAdShowEvent(
                    agent,
                    mediationAdapter,
                    AdShowResult.MIN_TIME_NOT_ELAPSED);
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen(
                        "Minimum interval between ads not elapsed");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen(
                        "Minimum interval between ads not elapsed");
            }
            return;
        }
        
        if (sessionAdCount >= adMaxPerSession) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Number of ads shown this session exceeded the maximum");
            
            postAdShowEvent(
                    agent,
                    mediationAdapter,
                    AdShowResult.SESSION_LIMIT_REACHED);
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen(
                        "Too many ads shown per session");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen(
                        "Too many ads shown per session");
            }
            return;
        }
        
        if (    !TextUtils.isEmpty(adPoint)
                && adConfiguration.optBoolean("adShowPoint", true)) {
            final MediationAdapter finalMediationAdapter = mediationAdapter;
            
            final EngagementListener engageListener = new EngagementListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    JSONObject parameters = result.optJSONObject("parameters");
                    if (parameters != null) {
                        if (parameters.optBoolean("adShowPoint", true)) {
                            Log.d(BuildConfig.LOG_TAG, "Engage allowing ad at adPoint " + adPoint);
                            if (agent.isAdLoaded()) {
                                try {
                                    postAdShowEvent(agent, finalMediationAdapter, AdShowResult.FULFILLED);
                                    agent.showAd(adPoint);
                                } catch (Exception e) {
                                    Log.w(BuildConfig.LOG_TAG,
                                            "Error showing ad at adPoint " + adPoint,
                                            e);
                                    
                                    postAdShowEvent(
                                            agent,
                                            finalMediationAdapter,
                                            AdShowResult.INTERNAL_ERROR);
                                    if (agent.equals(interstitialAgent)) {
                                        listener.onInterstitialAdFailedToOpen(
                                                e.getMessage());
                                    } else if (agent.equals(rewardedAgent)) {
                                        listener.onRewardedAdFailedToOpen(
                                                e.getMessage());
                                    }
                                }
                            } else {
                                Log.w(BuildConfig.LOG_TAG, "Ad agent not ready");
                                
                                // inject ad point so still appears in event
                                agent.setAdPoint(adPoint);
                                
                                postAdShowEvent(
                                        agent,
                                        finalMediationAdapter,
                                        AdShowResult.NOT_READY);
                                if (agent.equals(interstitialAgent)) {
                                    listener.onInterstitialAdFailedToOpen(
                                            "Interstitial ad agent not ready");
                                } else if (agent.equals(rewardedAgent)) {
                                    listener.onRewardedAdFailedToOpen(
                                            "Rewarded ad agent not ready");
                                }
                            }
                        } else {
                            Log.w(BuildConfig.LOG_TAG,
                                    "Engage prevented ad from opening");
                            
                            // inject ad point so still appears in event
                            agent.setAdPoint(adPoint);
                            
                            postAdShowEvent(
                                    agent,
                                    finalMediationAdapter,
                                    AdShowResult.AD_SHOW_POINT);
                            if (agent.equals(interstitialAgent)) {
                                listener.onInterstitialAdFailedToOpen(
                                        "Ad agent not ready");
                            } else if (agent.equals(rewardedAgent)) {
                                listener.onRewardedAdFailedToOpen(
                                        "Ad agent not ready");
                            }
                        }
                    }
                }
                
                @Override
                public void onFailure(Throwable t) {
                    Log.w(BuildConfig.LOG_TAG,
                            "Engage request failed, showing ad anyway",
                            t);
                    
                    try {
                        postAdShowEvent(
                                agent,
                                finalMediationAdapter,
                                AdShowResult.FULFILLED);
                        agent.showAd(adPoint);
                    } catch (Exception e1) {
                        Log.w(BuildConfig.LOG_TAG,
                                "Error showing ad at adPoint " + adPoint,
                                e1);
                        
                        // inject ad point so still appears in event
                        agent.setAdPoint(adPoint);
                        
                        postAdShowEvent(
                                agent,
                                finalMediationAdapter,
                                AdShowResult.INTERNAL_ERROR);
                        if (agent.equals(interstitialAgent)) {
                            listener.onInterstitialAdFailedToOpen(
                                    e1.getMessage());
                        } else if (agent.equals(rewardedAgent)) {
                            listener.onRewardedAdFailedToOpen(
                                    e1.getMessage());
                        }
                    }
                }
            };
            
            listener.onRequestEngagement(
                    adPoint,
                    EngagementFlavour.ADVERTISING.toString(),
                    engageListener);
        } else if (!TextUtils.isEmpty(adPoint)) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Ad points not supported by configuration");
            
            postAdShowEvent(
                    agent,
                    mediationAdapter,
                    AdShowResult.AD_SHOW_POINT);
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen(
                        "Ad points not supported by configuration");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen(
                        "Ad points not supported by configuration");
            }
        } else {
            postAdShowEvent(agent, mediationAdapter, AdShowResult.FULFILLED);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    agent.showAd(null);
                }
            });
        }
    }
    
    private void requestAdConfiguration() {
        listener.onRequestEngagement(
                decisionPoint,
                EngagementFlavour.INTERNAL.toString(),
                new ConfigurationListener());
    }
    
    private void postAdShowEvent(AdAgent adAgent, MediationAdapter mediationAdapter, AdShowResult adShowResult) {
        String adType = AD_TYPE_UNKNOWN;
        if (adAgent != null && adAgent.equals(interstitialAgent)) {
            adType = AD_TYPE_INTERSTITIAL;
        } else if(adAgent != null && adAgent.equals(rewardedAgent)) {
            adType = AD_TYPE_REWARDED;
        }
        
        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("adProvider", mediationAdapter != null ? mediationAdapter.getProviderString() : "N/A");
            eventParams.put("adProviderVersion", mediationAdapter != null ? mediationAdapter.getProviderVersionString() : "N/A");
            eventParams.put("adType", adType);
            eventParams.put("adStatus", adShowResult.getStatus());
            eventParams.put("adSdkVersion", VERSION);
            eventParams.put("adPoint", adAgent != null ? adAgent.getAdPoint() : null);
        } catch (JSONException e) {
            Log.e(BuildConfig.LOG_TAG, e.getMessage());
        }
        
        Log.v(BuildConfig.LOG_TAG, "Posting adShow event: " + eventParams);
        
        listener.onRecordEvent("adShow", eventParams.toString());
    }
    
    private void postAdClosedEvent(AdAgent adAgent, MediationAdapter mediationAdapter, AdClosedResult adClosedResult) {
        String adType = AD_TYPE_UNKNOWN;
        if (adAgent != null && adAgent.equals(interstitialAgent)) {
            adType = AD_TYPE_INTERSTITIAL;
        } else if(adAgent != null && adAgent.equals(rewardedAgent)) {
            adType = AD_TYPE_REWARDED;
        }
        
        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("adProvider", mediationAdapter != null ? mediationAdapter.getProviderString() : "N/A");
            eventParams.put("adProviderVersion", mediationAdapter != null ? mediationAdapter.getProviderVersionString() : "N/A");
            eventParams.put("adType", adType);
            eventParams.put("adClicked", adAgent != null && adAgent.adWasClicked());
            eventParams.put("adLeftApplication", adAgent != null && adAgent.adDidLeaveApplication());
            eventParams.put("adEcpm", mediationAdapter != null ? mediationAdapter.eCPM : 0);
            eventParams.put("adSdkVersion", VERSION);
            eventParams.put("adStatus", adClosedResult.status);
        } catch (JSONException e) {
            Log.e(BuildConfig.LOG_TAG, e.getMessage());
        }
        
        Log.v(BuildConfig.LOG_TAG, "Posting adClosed event: " + eventParams);
        
        listener.onRecordEvent("adClosed", eventParams.toString());
    }
    
    private void postAdRequestEventSuccess(AdAgent adAgent, MediationAdapter mediationAdapter, long requestDuration) {
        postAdRequestEvent(adAgent, mediationAdapter, requestDuration, null, AdRequestResult.Loaded);
    }
    
    private void postAdRequestEvent(AdAgent adAgent, MediationAdapter mediationAdapter, long requestDuration, String errorReason, AdRequestResult adLoadResult) {
        if(!adDebugMode) {
            return;
        }
        
        String adType = AD_TYPE_UNKNOWN;
        if (adAgent != null && adAgent.equals(interstitialAgent)) {
            adType = AD_TYPE_INTERSTITIAL;
        } else if(adAgent != null && adAgent.equals(rewardedAgent)) {
            adType = AD_TYPE_REWARDED;
        }
        
        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("adProvider", mediationAdapter != null ? mediationAdapter.getProviderString() : "N/A");
            eventParams.put("adProviderVersion", mediationAdapter != null ? mediationAdapter.getProviderVersionString() : "N/A");
            eventParams.put("adType", adType);
            eventParams.put("adSdkVersion", VERSION);
            eventParams.put("adRequestTimeMs", requestDuration);
            eventParams.put("adWaterfallIndex", mediationAdapter != null ? mediationAdapter.getWaterfallIndex() : -1);
            eventParams.put("adStatus", adLoadResult);
            if(errorReason != null) {
                eventParams.put("adProviderError", errorReason);
            }
        } catch (JSONException e) {
            Log.e(BuildConfig.LOG_TAG, e.getMessage());
        }
        
        Log.v(BuildConfig.LOG_TAG, "Posting adRequest event: " + eventParams);
        
        listener.onRecordEvent("adRequest", eventParams.toString());
    }
    
    private final class AgentListener implements AdAgentListener {
        
        @Override
        public void onAdLoaded(
                AdAgent agent,
                MediationAdapter adapter,
                long time) {
            
            if (agent.equals(interstitialAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Interstitial ad loaded");
                postAdRequestEventSuccess(agent, adapter, time);
            } else if (agent.equals(rewardedAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Rewarded ad loaded");
                postAdRequestEventSuccess(agent, adapter, time);
            }
        }
        
        @Override
        public void onAdFailedToLoad(
                AdAgent agent,
                MediationAdapter adapter,
                String reason,
                long time,
                AdRequestResult result) {
            
            if (agent.equals(interstitialAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Interstitial ad failed to load: " + reason);
                
                postAdRequestEvent(agent, adapter, time, reason, result);
            } else if (agent.equals(rewardedAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Rewarded ad failed to load: " + reason);
                
                postAdRequestEvent(agent, adapter, time, reason, result);
            }
        }
        
        @Override
        public void onAdOpened(AdAgent agent, MediationAdapter adapter) {
            sessionAdCount++;
            
            if (agent.equals(interstitialAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Interstitial ad opened");
                listener.onInterstitialAdOpened();
            } else if (agent.equals(rewardedAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Rewarded ad opened");
                listener.onRewardedAdOpened();
            }
        }
        
        @Override
        public void onAdFailedToOpen(
                AdAgent agent,
                MediationAdapter adapter,
                String reason,
                AdClosedResult result) {
            
            if (agent.equals(interstitialAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Interstitial ad failed to open: " + reason);
                
                postAdClosedEvent(agent, adapter, result);
                listener.onInterstitialAdFailedToOpen(reason);
            } else if (agent.equals(rewardedAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Rewarded ad failed to open: " + reason);
                
                postAdClosedEvent(agent, adapter, result);
                listener.onRewardedAdFailedToOpen(reason);
            }
        }
        
        @Override
        public void onAdClosed(
                AdAgent agent,
                MediationAdapter adapter,
                boolean complete) {
            
            lastAdShownTime = System.currentTimeMillis();
            if (agent.equals(interstitialAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Interstitial ad closed");
                
                postAdClosedEvent(agent, adapter, AdClosedResult.SUCCESS);
                listener.onInterstitialAdClosed();
            } else if (agent.equals(rewardedAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Rewarded ad closed");
                
                postAdClosedEvent(agent, adapter, AdClosedResult.SUCCESS);
                listener.onRewardedAdClosed(complete);
            }
        }
    }
    
    private final class ConfigurationListener implements EngagementListener {
        
        @Override
        public void onSuccess(JSONObject result) {
            Log.d(BuildConfig.LOG_TAG, "Engage request succeeded: " + result);
            
            if (!result.has("parameters")) {
                listener.onFailedToRegisterForInterstitialAds(
                        "Invalid Engage response, missing 'parameters' key");
                listener.onFailedToRegisterForRewardedAds(
                        "Invalid Engage response, missing 'parameters' key");
                return;
            }
            
            adConfiguration = result.optJSONObject("parameters");
            
            if (!adConfiguration.optBoolean("adShowSession", false)) {
                listener.onFailedToRegisterForInterstitialAds(
                        "Ads disabled for this session");
                listener.onFailedToRegisterForRewardedAds(
                        "Ads disabled for this session");
                return;
            }
            
            if (    !adConfiguration.has("adProviders")
                    && !adConfiguration.has("adRewardedProviders")) {
                listener.onFailedToRegisterForInterstitialAds(
                        "Invalid Engage response, missing 'adProviders' key");
                listener.onFailedToRegisterForRewardedAds(
                        "Invalid Engage response, missing 'adRewardedProviders' key");
                return;
            }
            
            adDebugMode = adConfiguration.optBoolean("adRecordAdRequests", true);
            
            final int adFloorPrice = adConfiguration.optInt("adFloorPrice");
            final int demoteOnCode = adConfiguration.optInt("onDemoteRequestCode");
            final int maxPerNetwork = adConfiguration.optInt("maxPerNetwork");
            adMinimumInterval = adConfiguration.optInt("adMinimumInterval");
            adMaxPerSession = adConfiguration.optInt("adMaxPerSession");
            
            final JSONArray interstitialProviders =
                    adConfiguration.optJSONArray("adProviders");
            if (interstitialProviders != null && interstitialProviders.length() > 0) {
                final Waterfall waterfall = WaterfallFactory.create(
                        interstitialProviders,
                        adFloorPrice,
                        demoteOnCode,
                        AdProviderType.INTERSTITIAL);
                
                if (waterfall.getAdapters().isEmpty()) {
                    listener.onFailedToRegisterForInterstitialAds(
                            "Invalid ad configuration");
                } else {
                    interstitialAgent = new AdAgent(
                            agentListener,
                            waterfall,
                            maxPerNetwork);
                    interstitialAgent.requestAd(activity, adConfiguration);
                    
                    listener.onRegisteredForInterstitialAds();
                }
            } else {
                listener.onFailedToRegisterForInterstitialAds(
                        "No interstitial ad providers defined");
            }
            
            final JSONArray rewardedProviders =
                    adConfiguration.optJSONArray("adRewardedProviders");
            if (rewardedProviders != null && rewardedProviders.length() > 0) {
                final Waterfall waterfall = WaterfallFactory.create(
                        rewardedProviders,
                        adFloorPrice,
                        demoteOnCode,
                        AdProviderType.REWARDED);
                
                if (waterfall.getAdapters().isEmpty()) {
                    listener.onFailedToRegisterForRewardedAds(
                            "Invalid ad configuration");
                } else {
                    rewardedAgent = new AdAgent(
                            agentListener,
                            waterfall,
                            maxPerNetwork);
                    rewardedAgent.requestAd(activity, adConfiguration);

                    listener.onRegisteredForRewardedAds();
                }
            } else {
                listener.onFailedToRegisterForRewardedAds(
                        "No rewarded ad providers defined");
            }
        }
        
        @Override
        public void onFailure(Throwable t) {
            Log.w(BuildConfig.LOG_TAG, "Engage request failed", t);
            
            final NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) {
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(  BuildConfig.LOG_TAG,
                                        "Retrying to register for ads");
                                requestAdConfiguration();
                            }
                        },
                        TIME_ONE_SECOND);
            } else {
                listener.onFailedToRegisterForInterstitialAds(
                        "Failed to initialise ad service");
                listener.onFailedToRegisterForRewardedAds(
                        "Failed to initialise ad service");
            }
        }
    }
}
