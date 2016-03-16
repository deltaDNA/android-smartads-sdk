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
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.core.engage.EngagementListener;
import com.deltadna.android.sdk.ads.core.engage.EngagementFlavour;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdService implements
        EngagementListener,
        AdAgentListener {
    
    public static final String AD_TYPE_UNKNOWN = "UNKNOWN";
    public static final String AD_TYPE_INTERSTITIAL = "INTERSTITIAL";
    public static final String AD_TYPE_REWARDED = "REWARDED";

    public static final int AD_CONFIGURATION_RETRY_SECONDS = 60 * 15;
    
    static final String VERSION = "SmartAds v" + BuildConfig.VERSION_NAME;
    
    private final Activity activity;
    private final AdServiceListener listener;
    
    private String decisionPoint;
    
    private JSONObject adConfiguration;
    
    private AdAgent interstitialAgent;
    private AdAgent rewardedAgent;

    private int sessionAdCount = 0;
    private long lastAdShownTime = 0;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int adMinimumInterval;
    private int adMaxPerSession;
    private boolean adDebugMode = true;

    public AdService(Activity activity, AdServiceListener listener) {
        Preconditions.checkArg(activity != null, "activity cannot be null");
        Preconditions.checkArg(listener != null, "listener cannot be null");
        
        this.activity = activity;
        this.listener = listener;
    }

    public void init(String decisionPoint) {
        Log.d(  BuildConfig.LOG_TAG,
                "Initialising AdService version " + VERSION);

        this.decisionPoint = decisionPoint;
        
        requestAdConfiguration();
    }
    
    @Override
    public void onSuccess(JSONObject result) {
        Log.d(BuildConfig.LOG_TAG, "Engage request succeeded: " + result);

        if (!result.has("parameters")) {
            listener.onFailedToRegisterForAds("Invalid Engage response, missing 'parameters' key.");
            listener.onFailedToRegisterForRewardedAds("Invalid Engage response, missing 'parameters' key.");
            return;
        }

        adConfiguration = result.optJSONObject("parameters");

        if (!adConfiguration.optBoolean("adShowSession", false)) {
            listener.onFailedToRegisterForAds("Ads disabled for this session");
            listener.onFailedToRegisterForRewardedAds("Ads disabled for this session");
            return;
        }

        if (!adConfiguration.has("adProviders") && !adConfiguration.has("adRewardedProviders")) {
            listener.onFailedToRegisterForAds("Invalid Engage response, missing 'adProviders' key.");
            listener.onFailedToRegisterForRewardedAds("Invalid Engage response, missing 'adRewardedProviders' key.");
            return;
        }

        adDebugMode = adConfiguration.optBoolean("adRecordAdRequests", true);

        final int adFloorPrice = adConfiguration.optInt("adFloorPrice");
        final int demoteOnCode = adConfiguration.optInt("onDemoteRequestCode");
        final int maxPerNetwork = adConfiguration.optInt("maxPerNetwork");
        adMinimumInterval = adConfiguration.optInt("adMinimumInterval");
        adMaxPerSession = adConfiguration.optInt("adMaxPerSession");

        JSONArray adProviders = adConfiguration.optJSONArray("adProviders");
        if(adProviders != null && adProviders.length() > 0) {
            final Waterfall waterfall = WaterfallFactory.create(
                    adProviders,
                    adFloorPrice,
                    demoteOnCode,
                    AdProviderType.INTERSTITIAL);
            
            if (waterfall.getAdapters().isEmpty()) {
                listener.onFailedToRegisterForAds("Invalid ad configuration");
            } else {
                interstitialAgent = new AdAgent(
                        this,
                        waterfall,
                        maxPerNetwork);
                interstitialAgent.requestAd(activity, adConfiguration);
                
                listener.onRegisteredForAds();
            }
        } else {
            listener.onFailedToRegisterForAds("No interstitial ad providers defined");
        }
        
        JSONArray adRewardedProviders = adConfiguration.optJSONArray("adRewardedProviders");
        if(adRewardedProviders != null && adRewardedProviders.length() > 0) {
            final Waterfall waterfall = WaterfallFactory.create(
                            adRewardedProviders,
                            adFloorPrice,
                            demoteOnCode,
                            AdProviderType.REWARDED);
            
            if (waterfall.getAdapters().isEmpty()) {
                listener.onFailedToRegisterForRewardedAds("Invalid ad configuration");
            } else {
                rewardedAgent = new AdAgent(
                        this,
                        waterfall,
                        maxPerNetwork);
                rewardedAgent.requestAd(activity, adConfiguration);
                
                listener.onRegisteredForRewardedAds();
            }
        } else {
            listener.onFailedToRegisterForRewardedAds("No rewarded ad providers defined");
        }
    }
    
    @Override
    public void onFailure(Throwable t) {
        Log.w(BuildConfig.LOG_TAG, "Engage request failed due to " + t, t);

        listener.onFailedToRegisterForAds("Engage request failed, unable to initialise ad service.");

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                requestAdConfiguration();
            }
        }, AD_CONFIGURATION_RETRY_SECONDS, TimeUnit.SECONDS);
    }
    
    @Override
    public void onAdLoaded(AdAgent adAgent, MediationAdapter mediationAdapter, long requestTime) {
        if (adAgent.equals(interstitialAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Interstitial ad loaded");
            postAdRequestEventSuccess(adAgent, mediationAdapter, requestTime);
        } else if(adAgent.equals(rewardedAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Rewarded ad loaded");
            postAdRequestEventSuccess(adAgent, mediationAdapter, requestTime);
        }
    }

    @Override
    public void onAdFailedToLoad(AdAgent adAgent, MediationAdapter mediationAdapter, String reason, long requestTime, AdRequestResult adLoadResult) {
        if (adAgent.equals(interstitialAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Interstitial ad failed to load: " + reason);
            postAdRequestEvent(adAgent, mediationAdapter, requestTime, reason, adLoadResult);
        } else if(adAgent.equals(rewardedAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Rewarded ad failed to load: " + reason);
            postAdRequestEvent(adAgent, mediationAdapter, requestTime, reason, adLoadResult);
        }
    }

    @Override
    public void onAdOpened(AdAgent adAgent, MediationAdapter mediationAdapter) {
        sessionAdCount++;
        if (adAgent.equals(interstitialAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Interstitial ad opened");
            listener.onAdOpened();
        } else if(adAgent.equals(rewardedAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Rewarded ad opened");
            listener.onRewardedAdOpened();
        }
    }

    @Override
    public void onAdFailedToOpen(AdAgent adAgent, MediationAdapter mediationAdapter, String reason, AdClosedResult adClosedResult) {
        if (adAgent.equals(interstitialAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Interstitial ad failed to open");
            postAdClosedEvent(adAgent, mediationAdapter, adClosedResult);
            listener.onAdFailedToOpen();
        } else if(adAgent.equals(rewardedAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Rewarded ad failed to open");
            postAdClosedEvent(adAgent, mediationAdapter, adClosedResult);
            listener.onRewardedAdFailedToOpen();
        }
    }

    @Override
    public void onAdClosed(AdAgent adAgent, MediationAdapter mediationAdapter, boolean complete) {
        lastAdShownTime = System.currentTimeMillis();
        if (adAgent.equals(interstitialAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Interstitial ad closed");
            postAdClosedEvent(adAgent, mediationAdapter, AdClosedResult.SUCCESS);
            listener.onAdClosed();
        } else if(adAgent.equals(rewardedAgent)) {
            Log.d(BuildConfig.LOG_TAG, "Rewarded ad closed");
            postAdClosedEvent(adAgent, mediationAdapter, AdClosedResult.SUCCESS);
            listener.onRewardedAdClosed(complete);
        }
    }

    public void showAd() {
        if (interstitialAgent != null) {
            showAdImpl(interstitialAgent);
        } else {
            listener.onAdFailedToOpen();
        }
    }

    public void showAd(final String adPoint) {
        if (interstitialAgent != null) {
            showAdImpl(adPoint, interstitialAgent);
        } else {
            listener.onAdFailedToOpen();
        }
    }

    public void showRewardedAd() {
        if (rewardedAgent != null) {
            showAdImpl(rewardedAgent);
        } else {
            listener.onRewardedAdFailedToOpen();
        }
    }

    public void showRewardedAd(String adPoint) {
        if (rewardedAgent != null) {
            showAdImpl(adPoint, rewardedAgent);
        } else {
            listener.onRewardedAdFailedToOpen();
        }
    }

    private void showAdImpl(final AdAgent agent) {

        MediationAdapter mediationAdapter = null;
        if(agent != null) {
            mediationAdapter = agent.getCurrentAdapter();
            agent.setAdPoint(null);
        }

        if(System.currentTimeMillis() - lastAdShownTime <= adMinimumInterval) {
            Log.i(BuildConfig.LOG_TAG, "show ad called before minimum interval " + adMinimumInterval +" ms between ads has elapsed");
            postAdShowEvent(agent, mediationAdapter, AdShowResult.MIN_TIME_NOT_ELAPSED);
            listener.onAdFailedToOpen();
            return;
        }

        if(sessionAdCount >= adMaxPerSession) {
            Log.i(BuildConfig.LOG_TAG, "The number of ads shown during this session has exceeded the maximum: " + adMaxPerSession);
            postAdShowEvent(agent, mediationAdapter, AdShowResult.SESSION_LIMIT_REACHED);
            listener.onAdFailedToOpen();
            return;
        }

        if(agent != null && agent.isAdLoaded()) {
            try {
                postAdShowEvent(agent, mediationAdapter, AdShowResult.FULFILLED);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        agent.showAd(null);
                    }
                });
            } catch (Exception e) {
                Log.e(BuildConfig.LOG_TAG, "Problem showing ad", e);
                postAdShowEvent(interstitialAgent, interstitialAgent.getCurrentAdapter(), AdShowResult.INTERNAL_ERROR);
                listener.onAdFailedToOpen();
            }
        } else {
            postAdShowEvent(agent, mediationAdapter, agent != null ? AdShowResult.NOT_READY : AdShowResult.NOT_REGISTERED);
            listener.onAdFailedToOpen();
        }

    }

    private void showAdImpl(final String adPoint, final AdAgent agent) {
        MediationAdapter mediationAdapter = null;
        if(agent != null) {
            mediationAdapter = agent.getCurrentAdapter();
            agent.setAdPoint(adPoint);
        }

        if(System.currentTimeMillis() - lastAdShownTime <= adMinimumInterval) {
            Log.i(BuildConfig.LOG_TAG, "show ad called before minimum interval " + adMinimumInterval +" ms between ads has elapsed");
            postAdShowEvent(agent, mediationAdapter, AdShowResult.MIN_TIME_NOT_ELAPSED);
            listener.onAdFailedToOpen();
            return;
        }

        if(sessionAdCount >= adMaxPerSession) {
            Log.i(BuildConfig.LOG_TAG, "The number of ads shown during this session has exceeded the maximum: " + adMaxPerSession);
            postAdShowEvent(agent, mediationAdapter, AdShowResult.SESSION_LIMIT_REACHED);
            listener.onAdFailedToOpen();
            return;
        }

        if (agent != null) {
            if (adConfiguration.optBoolean("adShowPoint", true)) {
                final MediationAdapter finalMediationAdapter = mediationAdapter;
                
                final EngagementListener engageListener = new EngagementListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        JSONObject parameters = result.optJSONObject("parameters");
                        if (parameters != null) {
                            if (parameters.optBoolean("adShowPoint", true)) {
                                Log.d(BuildConfig.LOG_TAG, "Engage allowing ad at adPoint " + adPoint);
                                if(agent.isAdLoaded()) {
                                    try {
                                        postAdShowEvent(agent, finalMediationAdapter, AdShowResult.FULFILLED);
                                        agent.showAd(adPoint);
                                    } catch (Exception e) {
                                        Log.e(BuildConfig.LOG_TAG, "Problem showing ad at adPoint "+adPoint, e);
                                        postAdShowEvent(interstitialAgent, interstitialAgent.getCurrentAdapter(), AdShowResult.INTERNAL_ERROR);
                                        listener.onAdFailedToOpen();
                                    }
                                } else {
                                    agent.setAdPoint(adPoint);  // inject ad point so still appears in event
                                    postAdShowEvent(agent, finalMediationAdapter, AdShowResult.NOT_READY);
                                    listener.onAdFailedToOpen();
                                }
                            } else {
                                Log.d(BuildConfig.LOG_TAG, "Engage prevented ad from opening");
                                agent.setAdPoint(adPoint);  // inject ad point so still appears in event
                                postAdShowEvent(agent, finalMediationAdapter, AdShowResult.AD_SHOW_POINT);
                                listener.onAdFailedToOpen();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // Engage failed so we should try and show the ad
                        Log.d(BuildConfig.LOG_TAG, "Engage request failed: "+t+", showing ad anyway");
                        try {
                            postAdShowEvent(agent, finalMediationAdapter, AdShowResult.AD_SHOW_ENGAGE_FAILED);
                            agent.showAd(adPoint);
                        } catch (Exception e1) {
                            Log.e(BuildConfig.LOG_TAG, "Problem showing ad at adPoint "+adPoint, e1);
                            agent.setAdPoint(adPoint);  // inject ad point so still appears in event
                            postAdShowEvent(interstitialAgent, interstitialAgent.getCurrentAdapter(), AdShowResult.INTERNAL_ERROR);
                            listener.onAdFailedToOpen();
                        }
                    }
                };
                
                listener.onRequestEngagement(
                        adPoint,
                        EngagementFlavour.ADVERTISING.toString(),
                        engageListener);
            } else {
                Log.d(BuildConfig.LOG_TAG, "adShowPoint=false in configuration, not supporting adPoints");
                showAd();
            }
        } else {
            postAdShowEvent(null, null, AdShowResult.NOT_REGISTERED);
            listener.onAdFailedToOpen();
        }
    }

    public void onPause() {
        if (interstitialAgent != null) {
            interstitialAgent.onPause();
        }
        if(rewardedAgent != null) {
            rewardedAgent.onPause();
        }
    }

    public void onResume() {
        if (interstitialAgent != null) {
            interstitialAgent.onResume();
        }
        if(rewardedAgent != null) {
            rewardedAgent.onResume();
        }
    }

    public void onDestroy() {
        if (interstitialAgent != null) {
            interstitialAgent.onDestroy();
        }
        if(rewardedAgent != null) {
            rewardedAgent.onDestroy();
        }
    }
    
    private void requestAdConfiguration() {
        listener.onRequestEngagement(
                decisionPoint,
                EngagementFlavour.INTERNAL.toString(),
                this);
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
            eventParams.put("adStatus", adClosedResult.getStatus());
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

    public boolean isInterstitialAdAvailable() {
        return interstitialAgent != null && interstitialAgent.isAdLoaded();
    }

    public boolean isRewardedAdAvailable() {
        return rewardedAgent != null && rewardedAgent.isAdLoaded();
    }
}
