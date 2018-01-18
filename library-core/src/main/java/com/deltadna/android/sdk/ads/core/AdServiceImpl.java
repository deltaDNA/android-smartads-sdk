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
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.Actions;
import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class AdServiceImpl implements AdService {
    
    public static final String AD_TYPE_UNKNOWN = "UNKNOWN";
    public static final String AD_TYPE_INTERSTITIAL = "INTERSTITIAL";
    public static final String AD_TYPE_REWARDED = "REWARDED";
    
    private static final int TIME_ONE_SECOND = 60 * 1000;
    private static final String VERSION = "SmartAds v" + BuildConfig.VERSION_NAME;
    private static final String AD_SHOW_POINT = "adShowPoint";
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private final ExceptionHandler exceptionHandler;
    private final Activity activity;
    private final AdServiceListener listener;

    private final LocalBroadcastManager broadcasts;
    private final Set<AdAgentListener> adAgentListeners;
    
    private String decisionPoint;
    
    private JSONObject adConfiguration;
    
    private AdAgent interstitialAgent;
    private AdAgent rewardedAgent;
    
    private int adMinimumInterval;
    private int adMaxPerSession;
    
    private boolean adDebugMode = true;
    
    AdServiceImpl(
            Activity activity,
            AdServiceListener listener,
            String sdkVersion) {
        
        Preconditions.checkArg(activity != null, "activity cannot be null");
        Preconditions.checkArg(listener != null, "listener cannot be null");
        
        String version = "";
        int versionCode = -1;
        try {
            final PackageInfo info = activity
                    .getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0);
            
            version = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to read app versions", e);
        }
        exceptionHandler = new ExceptionHandler(
                activity,
                version,
                versionCode,
                sdkVersion,
                BuildConfig.VERSION_NAME);
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        
        this.activity = activity;
        this.listener = MainThread.redirect(listener, AdServiceListener.class);
        
        broadcasts = LocalBroadcastManager.getInstance(activity);
        adAgentListeners = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                new AgentListener(),
                new Broadcaster())));
        
        // dynamically load the DebugReceiver
        try {
            @SuppressWarnings("unchecked") final Class<BroadcastReceiver> cls =
                    (Class<BroadcastReceiver>) Class.forName(
                            "com.deltadna.android.sdk.ads.debug.DebugReceiver");
            broadcasts.registerReceiver(cls.newInstance(), Actions.FILTER);
            Log.d(BuildConfig.LOG_TAG, "DebugReceiver registered");
        }  catch (ClassNotFoundException ignored) {
            Log.d(BuildConfig.LOG_TAG, "DebugReceiver not found in classpath");
        } catch (IllegalAccessException e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to load DebugReceiver", e);
        } catch (InstantiationException e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to load DebugReceiver", e);
        }
    }
    
    @Override
    public void registerForAds(String decisionPoint) {
        Log.d(  BuildConfig.LOG_TAG,
                "Initialising AdService version " + VERSION);
        
        this.decisionPoint = decisionPoint;
        
        requestAdConfiguration();
    }
    
    @Override
    public void onSessionUpdated() {
        broadcasts.sendBroadcast(new Intent(Actions.SESSION_UPDATED));
    }
    
    @Override
    public boolean isInterstitialAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject engagementParameters) {
        
        return isAdAllowed(
                interstitialAgent,
                decisionPoint,
                engagementParameters);
    }
    
    @Override
    public boolean isRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject engagementParameters) {
        
        return isAdAllowed(
                rewardedAgent,
                decisionPoint,
                engagementParameters);
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (interstitialAgent != null) {
                    interstitialAgent.onPause();
                }
                if (rewardedAgent != null) {
                    rewardedAgent.onPause();
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (interstitialAgent != null) {
                    interstitialAgent.onResume();
                }
                if (rewardedAgent != null) {
                    rewardedAgent.onResume();
                }
            }
        });
    }
    
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (interstitialAgent != null) {
                    interstitialAgent.onDestroy();
                }
                if (rewardedAgent != null) {
                    rewardedAgent.onDestroy();
                }
            }
        });
    }
    
    private boolean isAdAllowed(
            @Nullable AdAgent agent,
            @Nullable String decisionPoint,
            @Nullable JSONObject engagementParameters) {
        
        if (agent == null) {
            return false;
        }
        
        agent.setAdPoint(!TextUtils.isEmpty(decisionPoint)
                ? decisionPoint
                : null);
        
        if (!adConfiguration.optBoolean("adShowPoint", true)) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Ad points not supported by configuration");
            postAdShowEvent(
                    agent,
                    agent.getCurrentAdapter(),
                    AdShowResult.AD_SHOW_POINT);
            return false;
        }
        
        if (    engagementParameters != null
                && !engagementParameters.optBoolean(AD_SHOW_POINT, true)) {
            
            Log.w(  BuildConfig.LOG_TAG,
                    "Engage prevented ad from opening at " + decisionPoint);
            postAdShowEvent(
                    agent,
                    agent.getCurrentAdapter(),
                    AdShowResult.AD_SHOW_POINT);
            return false;
        }
        
        if (    adMinimumInterval != -1 &&
                System.currentTimeMillis() - agent.lastShownTime
                <= adMinimumInterval * 1000) {
            
            Log.w(BuildConfig.LOG_TAG, "Not showing ad before minimum interval");
            postAdShowEvent(
                    agent,
                    agent.getCurrentAdapter(),
                    AdShowResult.MIN_TIME_NOT_ELAPSED);
            return false;
        }
        
        if (adMaxPerSession != -1 && agent.shownCount >= adMaxPerSession) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Number of ads shown this session exceeded the maximum");
            postAdShowEvent(
                    agent,
                    agent.getCurrentAdapter(),
                    AdShowResult.SESSION_LIMIT_REACHED);
            return false;
        }
        
        if (!agent.isAdLoaded()) {
            Log.w(BuildConfig.LOG_TAG, "No ad available");
            postAdShowEvent(
                    agent,
                    agent.getCurrentAdapter(),
                    AdShowResult.NO_AD_AVAILABLE);
            return false;
        }
        
        Log.d(BuildConfig.LOG_TAG, "Ad fulfilled");
        postAdShowEvent(
                agent,
                agent.getCurrentAdapter(),
                AdShowResult.FULFILLED);
        return true;
    }
    
    private void showAd(final AdAgent agent, @Nullable final String adPoint) {
        if (!adConfiguration.optBoolean("adShowPoint", true)) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Ad points not supported by configuration");
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen(
                        "Ad points not supported by configuration");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen(
                        "Ad points not supported by configuration");
            }
            return;
        }
        
        if (    adMinimumInterval != -1 &&
                System.currentTimeMillis() - agent.lastShownTime
                <= adMinimumInterval * 1000) {
            
            Log.w(BuildConfig.LOG_TAG, "Not showing ad before minimum interval");
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen("Too soon");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen("Too soon");
            }
            return;
        }
        
        if (adMaxPerSession != -1 && agent.shownCount >= adMaxPerSession) {
            Log.w(  BuildConfig.LOG_TAG,
                    "Number of ads shown this session exceeded the maximum");
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen("Session limit reached");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen("Session limit reached");
            }
            return;
        }
        
        if (!agent.isAdLoaded()) {
            Log.w(BuildConfig.LOG_TAG, "No ad loaded by agent");
            if (agent.equals(interstitialAgent)) {
                listener.onInterstitialAdFailedToOpen("Not ready");
            } else if (agent.equals(rewardedAgent)) {
                listener.onRewardedAdFailedToOpen("Not ready");
            }
            return;
        }
        
        if (!TextUtils.isEmpty(adPoint)) {
            final EngagementListener requestListener = new EngagementListener() {
                @Override
                public void onSuccess(JSONObject result) {
                    if (isAdAllowed(
                            agent,
                            adPoint,
                            result.optJSONObject("parameters"))) {
                        
                        showAd(agent, null);
                    } else if (agent == interstitialAgent) {
                        listener.onInterstitialAdFailedToOpen("Not allowed");
                    } else if (agent == rewardedAgent) {
                        listener.onRewardedAdFailedToOpen("Not allowed");
                    }
                }
                
                @Override
                public void onFailure(Throwable t) {
                    Log.w(  BuildConfig.LOG_TAG,
                            "Engage request failed, showing ad anyway",
                            t);
                    
                    if (isAdAllowed(agent, adPoint, null)) {
                        showAd(agent, null);
                    } else if (agent.equals(interstitialAgent)) {
                        listener.onInterstitialAdFailedToOpen("Not allowed");
                    } else if (agent.equals(rewardedAgent)) {
                        listener.onRewardedAdFailedToOpen("Not allowed");
                    }
                }
            };
            
            listener.onRequestEngagement(
                    adPoint,
                    EngagementFlavour.ADVERTISING.toString(),
                    null,
                    requestListener);
        } else {
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
                VERSION,
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
            if (!TextUtils.isEmpty(errorReason)) {
                eventParams.put(
                        "adProviderError",
                        errorReason.substring(0, Math.min(512, errorReason.length())));
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
                Log.w(  BuildConfig.LOG_TAG,
                        "No configuration returned by Engage due to missing 'parameters' key");
                
                broadcasts.sendBroadcast(
                        new Intent(Actions.FAILED_TO_REGISTER).putExtra(
                                Actions.REASON,
                                "No configuration returned by Engage due to missing 'parameters' key"));
                scheduleConfigurationRequest();
                
                return;
            }
            
            adConfiguration = result.optJSONObject("parameters");
            
            if (!adConfiguration.optBoolean("adShowSession", false)) {
                listener.onFailedToRegisterForInterstitialAds(
                        "Ads disabled for this session");
                listener.onFailedToRegisterForRewardedAds(
                        "Ads disabled for this session");
                broadcasts.sendBroadcast(
                        new Intent(Actions.FAILED_TO_REGISTER).putExtra(
                                Actions.REASON,
                                "Ads disabled for this session"));
                return;
            }
            
            if (    !adConfiguration.has("adProviders")
                    && !adConfiguration.has("adRewardedProviders")) {
                listener.onFailedToRegisterForInterstitialAds(
                        "Invalid Engage response, missing 'adProviders' key");
                listener.onFailedToRegisterForRewardedAds(
                        "Invalid Engage response, missing 'adRewardedProviders' key");
                broadcasts.sendBroadcast(
                        new Intent(Actions.FAILED_TO_REGISTER).putExtra(
                                Actions.REASON,
                                "Missing providers key in Engage response"));
                return;
            }
            
            adDebugMode = adConfiguration.optBoolean("adRecordAdRequests", true);
            
            final int adFloorPrice = adConfiguration.optInt("adFloorPrice");
            final int demoteOnCode = adConfiguration.optInt("adDemoteOnRequestCode");
            final int maxPerNetwork = adConfiguration.optInt("adMaxPerNetwork");
            adMinimumInterval = adConfiguration.optInt("adMinimumInterval", -1);
            adMaxPerSession = adConfiguration.optInt("adMaxPerSession", -1);
            
            final JSONArray interstitialProviders =
                    adConfiguration.optJSONArray("adProviders");
            if (interstitialProviders != null && interstitialProviders.length() > 0) {
                final Waterfall waterfall = WaterfallFactory.create(
                        interstitialProviders,
                        adFloorPrice,
                        demoteOnCode,
                        maxPerNetwork,
                        AdProviderType.INTERSTITIAL);
                
                if (waterfall.adapters.isEmpty()) {
                    Log.w(BuildConfig.LOG_TAG, "No interstitial ad networks enabled");
                    
                    listener.onFailedToRegisterForInterstitialAds(
                            "No interstitial ad networks enabled");
                    broadcasts.sendBroadcast(new Intent(Actions.FAILED_TO_REGISTER)
                            .putExtra(
                                    Actions.AGENT,
                                    Actions.Agent.INTERSTITIAL)
                            .putExtra(
                                    Actions.REASON,
                                    "No interstitial ad networks enabled"));
                } else {
                    interstitialAgent = new AdAgent(
                            adAgentListeners,
                            waterfall,
                            adMaxPerSession,
                            exceptionHandler);
                    interstitialAgent.requestAd(activity, adConfiguration);
                    
                    listener.onRegisteredForInterstitialAds();
                }
            } else {
                listener.onFailedToRegisterForInterstitialAds(
                        "No interstitial ad networks configured");
                broadcasts.sendBroadcast(new Intent(Actions.FAILED_TO_REGISTER)
                        .putExtra(
                                Actions.AGENT,
                                Actions.Agent.INTERSTITIAL)
                        .putExtra(
                                Actions.REASON,
                                "No interstitial ad providers defined"));
            }
            
            final JSONArray rewardedProviders =
                    adConfiguration.optJSONArray("adRewardedProviders");
            if (rewardedProviders != null && rewardedProviders.length() > 0) {
                final Waterfall waterfall = WaterfallFactory.create(
                        rewardedProviders,
                        adFloorPrice,
                        demoteOnCode,
                        maxPerNetwork,
                        AdProviderType.REWARDED);
                
                if (waterfall.adapters.isEmpty()) {
                    Log.w(BuildConfig.LOG_TAG, "No rewarded ad networks enabled");
                    
                    listener.onFailedToRegisterForRewardedAds(
                            "No rewarded ad networks enabled");
                    broadcasts.sendBroadcast(new Intent(Actions.FAILED_TO_REGISTER)
                            .putExtra(
                                    Actions.AGENT,
                                    Actions.Agent.REWARDED)
                            .putExtra(
                                    Actions.REASON,
                                    "No rewarded ad networks enabled"));
                } else {
                    rewardedAgent = new AdAgent(
                            adAgentListeners,
                            waterfall,
                            adMaxPerSession,
                            exceptionHandler);
                    rewardedAgent.requestAd(activity, adConfiguration);
                    
                    listener.onRegisteredForRewardedAds();
                }
            } else {
                listener.onFailedToRegisterForRewardedAds(
                        "No rewarded ad networks configured");
                broadcasts.sendBroadcast(new Intent(Actions.FAILED_TO_REGISTER)
                        .putExtra(
                                Actions.AGENT,
                                Actions.Agent.REWARDED)
                        .putExtra(
                                Actions.REASON,
                                "No rewarded ad networks configured"));
            }
        }
        
        @Override
        public void onFailure(Throwable t) {
            Log.w(BuildConfig.LOG_TAG, "Engage request failed: " + t);
            
            broadcasts.sendBroadcast(
                    new Intent(Actions.FAILED_TO_REGISTER).putExtra(
                            Actions.REASON,
                            "Engage request failed: " + t.getMessage()));
            scheduleConfigurationRequest();
        }
        
        private void scheduleConfigurationRequest() {
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
        }
    }
    
    private final class Broadcaster implements AdAgentListener {
        
        @Nullable
        private String lastShown;
        
        @Override
        public void onAdLoaded(
                AdAgent agent,
                MediationAdapter adapter,
                long requestTime) {
            
            if (agent == null) return;
            
            final String network = adapter.getProviderString();
            if (lastShown == null) {
                broadcasts.sendBroadcast(new Intent(Actions.LOADED)
                        .putExtra(Actions.AGENT, toAgent(agent))
                        .putExtra(Actions.NETWORK, network));
            } else {
                broadcasts.sendBroadcast(new Intent(Actions.SHOWN_AND_LOADED)
                        .putExtra(Actions.AGENT, toAgent(agent))
                        .putExtra(Actions.NETWORK_SHOWN, lastShown)
                        .putExtra(Actions.NETWORK_LOADED, network));
            }
        }
        
        @Override
        public void onAdFailedToLoad(
                AdAgent agent,
                MediationAdapter adapter,
                String reason,
                long requestTime,
                AdRequestResult result) {}
        
        @Override
        public void onAdOpened(
                AdAgent agent,
                MediationAdapter adapter) {
            
            if (agent == null) return;
            
            broadcasts.sendBroadcast(new Intent(Actions.SHOWING)
                    .putExtra(Actions.AGENT, toAgent(agent))
                    .putExtra(Actions.NETWORK, adapter.getProviderString()));
        }
        
        @Override
        public void onAdFailedToOpen(
                AdAgent agent,
                MediationAdapter adapter,
                String reason,
                AdClosedResult result) {}
        
        @Override
        public void onAdClosed(
                AdAgent agent,
                MediationAdapter adapter,
                boolean complete) {
            
            if (agent == null) return;
            
            broadcasts.sendBroadcast(new Intent(Actions.SHOWN)
                    .putExtra(Actions.AGENT, toAgent(agent))
                    .putExtra(Actions.NETWORK, adapter.getProviderString()));
            
            lastShown = adapter.getProviderString();
        }
        
        private Actions.Agent toAgent(AdAgent agent) {
            if (agent == interstitialAgent) {
                return Actions.Agent.INTERSTITIAL;
            } else {
                return Actions.Agent.REWARDED;
            }
        }
    }
}
