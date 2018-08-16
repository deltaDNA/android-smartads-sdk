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
import android.content.Context;
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
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MainThread;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.Privacy;
import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class AdServiceImpl implements AdService {
    
    private static final String VERSION = "SmartAds v" + BuildConfig.VERSION_NAME;
    
    private static final boolean DEFAULT_AD_SHOW_POINT = true;
    private static final boolean DEFAULT_AD_DEBUG_MODE = true;
    private static final int DEFAULT_AD_MINIMUM_INTERVAL = 0;
    private static final int DEFAULT_AD_MAX_PER_SESSION = -1;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private final ExceptionHandler exceptionHandler;
    private final Activity activity;
    private final AdServiceListener listener;
    
    private final AdMetrics metrics;
    private final LocalBroadcastManager broadcasts;
    private final Set<AdAgentListener> adAgentListeners;
    
    private Privacy privacy;
    
    private AdAgent interstitialAgent;
    private AdAgent rewardedAgent;
    
    private boolean adDebugMode = DEFAULT_AD_DEBUG_MODE;
    
    private int adMinimumInterval = DEFAULT_AD_MINIMUM_INTERVAL;
    private int adMaxPerSession = DEFAULT_AD_MAX_PER_SESSION;
    
    AdServiceImpl(
            Activity activity,
            AdServiceListener listener,
            String sdkVersion) {
        
        Preconditions.checkArg(activity != null, "activity cannot be null");
        Preconditions.checkArg(listener != null, "listener cannot be null");
        
        Log.d(  BuildConfig.LOG_TAG,
                "Initialising AdService version " + VERSION);
        
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
                activity.getApplicationContext(),
                version,
                versionCode,
                sdkVersion,
                BuildConfig.VERSION_NAME);
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        
        this.activity = activity;
        this.listener = MainThread.redirect(listener, AdServiceListener.class);
        
        metrics = new AdMetrics(activity.getSharedPreferences(
                Preferences.METRICS.preferencesName(),
                Context.MODE_PRIVATE));
        broadcasts = LocalBroadcastManager.getInstance(activity.getApplicationContext());
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
    public void onNewSession() {
        metrics.newSession(new Date());
        broadcasts.sendBroadcast(new Intent(Actions.SESSION_UPDATED));
    }
    
    @Override
    public void configure(
            JSONObject configuration,
            boolean cached,
            boolean userConsent,
            boolean ageRestricted) {
        
        this.privacy = new Privacy(userConsent, ageRestricted);
        
        configure(configuration, cached);
    }
    
    @Override
    public boolean isInterstitialAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters,
            boolean checkTime) {
        
        return isAdAllowed(
                interstitialAgent,
                decisionPoint,
                parameters,
                checkTime);
    }
    
    @Override
    public boolean isRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters,
            boolean checkTime) {
        
        return isAdAllowed(
                rewardedAgent,
                decisionPoint,
                parameters,
                checkTime);
    }
    
    @Override
    public int timeUntilRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters) {
        
        return timeUntilAdAllowed(
                rewardedAgent,
                decisionPoint,
                parameters);
    }
    
    @Override
    public boolean hasLoadedInterstitialAd() {
        return interstitialAgent != null && interstitialAgent.hasLoadedAd();
    }
    
    @Override
    public boolean hasLoadedRewardedAd() {
        return rewardedAgent != null && rewardedAgent.hasLoadedAd();
    }
    
    @Override
    public void showInterstitialAd(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters) {
        
        if (interstitialAgent != null) {
            interstitialAgent.setDecisionPoint(decisionPoint);
            showAd(interstitialAgent, decisionPoint, parameters);
        } else {
            listener.onInterstitialAdFailedToOpen("Not registered");
        }
    }
    
    @Override
    public void showRewardedAd(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters) {
        
        if (rewardedAgent != null) {
            rewardedAgent.setDecisionPoint(decisionPoint);
            showAd(rewardedAgent, decisionPoint, parameters);
        } else {
            listener.onRewardedAdFailedToOpen("Not registered");
        }
    }
    
    @Override
    public Date getLastShown(String decisionPoint) {
        return metrics.lastShown(decisionPoint);
    }
    
    @Override
    public int getSessionCount(String decisionPoint) {
        return metrics.sessionCount(decisionPoint);
    }
    
    @Override
    public int getDailyCount(String decisionPoint) {
        return metrics.dailyCount(decisionPoint);
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
    
    private void configure(JSONObject configuration, boolean cached) {
        final JSONObject adConfiguration =
                configuration.optJSONObject("parameters");
        if (adConfiguration == null) {
            Log.w(BuildConfig.LOG_TAG, "No configuration found");
            
            broadcasts.sendBroadcast(new Intent(Actions.FAILED_TO_REGISTER)
                    .putExtra(Actions.REASON, "No configuration found"));
            return;
        }
        
        if (cached) {
            Log.v(BuildConfig.LOG_TAG, "Using cached configuration");
        } else {
            Log.v(BuildConfig.LOG_TAG, "Using live configuration");
        }
        
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
        
        adDebugMode = adConfiguration.optBoolean(
                "adRecordAdRequests", DEFAULT_AD_DEBUG_MODE);
        
        final int adFloorPrice = adConfiguration.optInt("adFloorPrice");
        final int demoteOnCode = adConfiguration.optInt("adDemoteOnRequestCode");
        final int maxPerNetwork = adConfiguration.optInt("adMaxPerNetwork");
        adMinimumInterval = adConfiguration.optInt(
                "adMinimumInterval", DEFAULT_AD_MINIMUM_INTERVAL);
        adMaxPerSession = adConfiguration.optInt(
                "adMaxPerSession", DEFAULT_AD_MAX_PER_SESSION);
        
        final JSONArray interstitialProviders =
                adConfiguration.optJSONArray("adProviders");
        if (interstitialProviders != null && interstitialProviders.length() > 0) {
            final Waterfall waterfall = WaterfallFactory.create(
                    interstitialProviders,
                    adFloorPrice,
                    demoteOnCode,
                    maxPerNetwork,
                    privacy,
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        interstitialAgent.requestAd(activity, adConfiguration);
                    }
                });
                
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
                    privacy,
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        rewardedAgent.requestAd(activity, adConfiguration);
                    }
                });
                
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
        
        metrics.newSession(new Date());
    }
    
    private boolean isAdAllowed(
            @Nullable AdAgent agent,
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters,
            boolean checkTime) {
        
        if (agent == null) {
            Log.d(BuildConfig.LOG_TAG, "Ads disabled for this session");
            return false;
        }
        
        // null decision point + null parameters == no engagement -> ok
        if (!TextUtils.isEmpty(decisionPoint) && parameters == null) {
            Log.d(  BuildConfig.LOG_TAG,
                    "Ad cannot be shown with an invalid Engagement");
            return false;
        } else if (TextUtils.isEmpty(decisionPoint) && parameters == null) {
            Log.w(BuildConfig.LOG_TAG, "Using an empty Engagement is deprecated");
            return true;
        }
        
        final AdShowResult result = getResult(
                agent,
                decisionPoint,
                parameters);
        
        final boolean allowed;
        switch (result) {
            case MIN_TIME_NOT_ELAPSED:
            case MIN_TIME_DECISION_POINT_NOT_ELAPSED:
            case NOT_LOADED:
                allowed = !checkTime;
                break;
                
            case FULFILLED:
                allowed = true;
                break;
                
            default:
                allowed = false;
        }
        
        return allowed;
    }
    
    private int timeUntilAdAllowed(
            AdAgent agent,
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters) {
        
        if (TextUtils.isEmpty(decisionPoint) || parameters == null) {
            return 0;
        }
        
        final Date now = new Date();
        final int wait = parameters.optInt("ddnaAdShowWaitSecs", 0);
        
        if (adMinimumInterval >= wait) {
            final int lastShown = (int) MILLISECONDS.toSeconds(
                    now.getTime() - agent.lastShownTime);
            if (lastShown < adMinimumInterval) {
                return (adMinimumInterval - lastShown);
            }
        } else {
            final Date lastShownDate = metrics.lastShown(decisionPoint);
            if (lastShownDate != null) {
                final int lastShown = (int) MILLISECONDS.toSeconds(
                        now.getTime() - lastShownDate.getTime());
                if (lastShown < wait) {
                    return (wait - lastShown);
                }
            }
        }
        
        return 0;
    }
    
    private void showAd(
            final AdAgent agent,
            @Nullable final String decisionPoint,
            @Nullable JSONObject parameters) {
        
        if (!TextUtils.isEmpty(decisionPoint) && parameters == null) {
            didFailToOpenAd(agent, "Invalid Engagement");
            return;
        } else if (TextUtils.isEmpty(decisionPoint) && parameters == null) {
            Log.w(BuildConfig.LOG_TAG, "Prefer showing ads with Engagements");
        }
        
        final AdShowResult result = getResult(
                agent,
                decisionPoint,
                parameters);
        switch (result) {
            case AD_SHOW_POINT:
                postAdShowEvent(agent, result);
                didFailToOpenAd(agent, result.status);
                return;
            
            case MIN_TIME_NOT_ELAPSED:
                postAdShowEvent(agent, result);
                didFailToOpenAd(
                        agent,
                        "Minimum environment time between ads not elapsed");
                return;
            
            case MIN_TIME_DECISION_POINT_NOT_ELAPSED:
                postAdShowEvent(agent, result);
                didFailToOpenAd(
                        agent,
                        "Minimum decision point time between ads not elapsed");
                return;
            
            case SESSION_LIMIT_REACHED:
                postAdShowEvent(agent, result);
                didFailToOpenAd(
                        agent,
                        "Session limit for environment reached");
                return;
            
            case SESSION_DECISION_POINT_LIMIT_REACHED:
                postAdShowEvent(agent, result);
                didFailToOpenAd(
                        agent,
                        "Session limit for decision point reached");
                return;
            
            case DAILY_DECISION_POINT_LIMIT_REACHED:
                postAdShowEvent(agent, result);
                didFailToOpenAd(agent, "Daily limit for decision point reached");
                return;
        }
        
        if (!agent.hasLoadedAd()) {
            postAdShowEvent(agent, AdShowResult.NOT_LOADED);
            didFailToOpenAd(agent, "Ad not loaded");
            return;
        }
        
        postAdShowEvent(agent, AdShowResult.FULFILLED);
        handler.post(new Runnable() {
            @Override
            public void run() {
                agent.showAd(decisionPoint);
            }
        });
    }
    
    private AdShowResult getResult(
            AdAgent agent,
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters) {
        
        if (    parameters != null
                && parameters.has("adShowPoint")
                && !parameters.optBoolean("adShowPoint", DEFAULT_AD_SHOW_POINT)) {
            return AdShowResult.AD_SHOW_POINT;
        }
        
        if (adMaxPerSession != -1 && agent.shownCount >= adMaxPerSession) {
            return AdShowResult.SESSION_LIMIT_REACHED;
        }
        
        if (    !TextUtils.isEmpty(decisionPoint)
                && parameters != null
                && parameters.has("ddnaAdSessionCount")) {
            final int value = parameters.optInt("ddnaAdSessionCount");
            if (metrics.sessionCount(decisionPoint) >= value) {
                return AdShowResult.SESSION_DECISION_POINT_LIMIT_REACHED;
            }
        }
        
        if (    !TextUtils.isEmpty(decisionPoint)
                && parameters != null
                && parameters.has("ddnaAdDailyCount")) {
            final int value = parameters.optInt("ddnaAdDailyCount");
            if (metrics.dailyCount(decisionPoint) >= value) {
                return AdShowResult.DAILY_DECISION_POINT_LIMIT_REACHED;
            }
        }
        
        final Date now = new Date();
        if (    MILLISECONDS.toSeconds(now.getTime() - agent.lastShownTime)
                < adMinimumInterval) {
            return AdShowResult.MIN_TIME_NOT_ELAPSED;
        }
        
        if (    !TextUtils.isEmpty(decisionPoint)
                && parameters != null
                && parameters.has("ddnaAdShowWaitSecs")) {
            final int wait = parameters.optInt("ddnaAdShowWaitSecs");
            final Date lastShown = metrics.lastShown(decisionPoint);
            if (lastShown != null && MILLISECONDS.toSeconds(
                    now.getTime() - lastShown.getTime()) < wait) {
                return AdShowResult.MIN_TIME_DECISION_POINT_NOT_ELAPSED;
            }
        }
        
        if (!agent.hasLoadedAd()) {
            return AdShowResult.NOT_LOADED;
        }
        
        return AdShowResult.FULFILLED;
    }
    
    private void postAdShowEvent(AdAgent agent, AdShowResult result) {
        
        final MediationAdapter provider = agent.getCurrentAdapter();
        
        final JSONObject params = new JSONObject();
        try {
            params.put("adType", getType(agent));
            if (!TextUtils.isEmpty(agent.getDecisionPoint())) {
                params.put("adPoint", agent.getDecisionPoint());
            }
            params.put("adProvider", provider != null ? provider.getProviderString() : "N/A");
            params.put("adProviderVersion", provider != null ? provider.getProviderVersionString() : "N/A");
            params.put("adStatus", result.status);
            params.put("adSdkVersion", VERSION);
        } catch (JSONException e) {
            Log.w(BuildConfig.LOG_TAG, "Failed to build adShow parameters", e);
            return;
        }
        
        Log.v(BuildConfig.LOG_TAG, "Posting adShow event: " + params);
        
        listener.onRecordEvent("adShow", params.toString());
    }
    
    private void postAdClosedEvent(AdAgent adAgent, MediationAdapter mediationAdapter) {
        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("adProvider", mediationAdapter != null ? mediationAdapter.getProviderString() : "N/A");
            eventParams.put("adProviderVersion", mediationAdapter != null ? mediationAdapter.getProviderVersionString() : "N/A");
            eventParams.put("adType", getType(adAgent));
            eventParams.put("adClicked", adAgent != null && adAgent.adWasClicked());
            eventParams.put("adLeftApplication", adAgent != null && adAgent.adDidLeaveApplication());
            eventParams.put("adEcpm", mediationAdapter != null ? mediationAdapter.eCPM : 0);
            eventParams.put("adSdkVersion", VERSION);
            eventParams.put("adStatus", "Success");
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
        
        JSONObject eventParams = new JSONObject();
        try {
            eventParams.put("adProvider", mediationAdapter != null ? mediationAdapter.getProviderString() : "N/A");
            eventParams.put("adProviderVersion", mediationAdapter != null ? mediationAdapter.getProviderVersionString() : "N/A");
            eventParams.put("adType", getType(adAgent));
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
    
    private void didFailToOpenAd(AdAgent agent, String reason) {
        if (agent.equals(interstitialAgent)) {
            listener.onInterstitialAdFailedToOpen(reason);
        } else if (agent.equals(rewardedAgent)) {
            listener.onRewardedAdFailedToOpen(reason);
        }
    }
    
    private String getType(@Nullable AdAgent agent) {
        if (agent != null && agent.equals(interstitialAgent)) {
            return "INTERSTITIAL";
        } else if (agent != null && agent.equals(rewardedAgent)) {
            return "REWARDED";
        } else {
            return "UNKNOWN";
        }
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
                listener.onRewardedAdLoaded();
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
                listener.onRewardedAdOpened(agent.getDecisionPoint());
            }
        }
        
        @Override
        public void onAdFailedToOpen(
                AdAgent agent,
                MediationAdapter adapter,
                String reason,
                AdShowResult result) {
            
            if (agent.equals(interstitialAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Interstitial ad failed to open: " + reason);
                
                postAdShowEvent(agent, result);
                listener.onInterstitialAdFailedToOpen(reason);
            } else if (agent.equals(rewardedAgent)) {
                Log.w(  BuildConfig.LOG_TAG,
                        "Rewarded ad failed to open: " + reason);
                
                postAdShowEvent(agent, result);
                listener.onRewardedAdFailedToOpen(reason);
            }
        }
        
        @Override
        public void onAdClosed(
                AdAgent agent,
                MediationAdapter adapter,
                boolean complete) {
            
            if (!TextUtils.isEmpty(agent.getDecisionPoint())) {
                metrics.recordAdShown(
                        agent.getDecisionPoint(),
                        new Date(agent.lastShownTime));
            }
            
            if (agent.equals(interstitialAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Interstitial ad closed");
                
                postAdClosedEvent(agent, adapter);
                listener.onInterstitialAdClosed();
            } else if (agent.equals(rewardedAgent)) {
                Log.d(BuildConfig.LOG_TAG, "Rewarded ad closed");
                
                postAdClosedEvent(agent, adapter);
                listener.onRewardedAdClosed(complete);
            }
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
                AdShowResult result) {}
        
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
