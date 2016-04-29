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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.util.Log;

import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.AdClosedResult;

class ChartBoostEventForwarder extends ChartboostDelegate {
    
    private MediationListener rewardedMediationListener;
    private MediationAdapter rewardedAdMediationAdapter;
    
    private MediationListener interstitialMediationListener;
    private MediationAdapter interstitialMediationAdapter;
    
    private boolean rewardedComplete;
    
    public void setRewardedListeners(MediationListener rewardedMediationListener, MediationAdapter rewardedAdMediationAdapter) {
        this.rewardedAdMediationAdapter = rewardedAdMediationAdapter;
        this.rewardedMediationListener = rewardedMediationListener;
    }

    public void setInterstitialListeners(MediationListener interstitialMediationListener, MediationAdapter interstitialMediationAdapter) {
        this.interstitialMediationListener = interstitialMediationListener;
        this.interstitialMediationAdapter = interstitialMediationAdapter;
    }


    @Override
    public void didCacheInterstitial(String location) {
        super.didCacheInterstitial(location);
        Log.d(BuildConfig.LOG_TAG, "ChartBoost interstitial ad loaded");
        if(interstitialMediationListener != null) {
            interstitialMediationListener.onAdLoaded(interstitialMediationAdapter);
        }
    }

    @Override
    public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
        super.didFailToLoadInterstitial(location, error);
        Log.d(BuildConfig.LOG_TAG, "ChartBoost interstitial ad failed to load");
        if(interstitialMediationListener != null) {
            AdRequestResult adStatus;
            switch(error) {
                case INTERNAL:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case INTERNET_UNAVAILABLE:
                    adStatus = AdRequestResult.Network;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case TOO_MANY_CONNECTIONS:
                    adStatus = AdRequestResult.Network;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case WRONG_ORIENTATION:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case FIRST_SESSION_INTERSTITIALS_DISABLED:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case NETWORK_FAILURE:
                    adStatus = AdRequestResult.Network;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case NO_AD_FOUND:
                    adStatus = AdRequestResult.NoFill;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case SESSION_NOT_STARTED:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case IMPRESSION_ALREADY_VISIBLE:
                    interstitialMediationListener.onAdFailedToShow(interstitialMediationAdapter, AdClosedResult.EXPIRED);
                    break;
                case NO_HOST_ACTIVITY:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case USER_CANCELLATION:
                    interstitialMediationListener.onAdFailedToShow(interstitialMediationAdapter, AdClosedResult.ERROR);
                    break;
                case INVALID_LOCATION:
                    adStatus = AdRequestResult.Configuration;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case VIDEO_UNAVAILABLE:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case VIDEO_ID_MISSING:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_PLAYING_VIDEO:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case INVALID_RESPONSE:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case ASSETS_DOWNLOAD_FAILURE:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_CREATING_VIEW:
                    interstitialMediationListener.onAdFailedToShow(interstitialMediationAdapter, AdClosedResult.ERROR);
                    break;
                case ERROR_DISPLAYING_VIEW:
                    interstitialMediationListener.onAdFailedToShow(interstitialMediationAdapter, AdClosedResult.ERROR);
                    break;
                case INCOMPATIBLE_API_VERSION:
                    adStatus = AdRequestResult.Configuration;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_LOADING_WEB_VIEW:
                    interstitialMediationListener.onAdFailedToShow(interstitialMediationAdapter, AdClosedResult.ERROR);
                    break;
                case ASSET_PREFETCH_IN_PROGRESS:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case EMPTY_LOCAL_AD_LIST:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case ACTIVITY_MISSING_IN_MANIFEST:
                    adStatus = AdRequestResult.Configuration;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                case EMPTY_LOCAL_VIDEO_LIST:
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
                default :
                    adStatus = AdRequestResult.Error;
                    interstitialMediationListener.onAdFailedToLoad(interstitialMediationAdapter, adStatus, error.toString());
                    break;
            }
        }
    }

    @Override
    public void didDismissInterstitial(String location) {
        if(interstitialMediationListener != null) {
            interstitialMediationListener.onAdClosed(interstitialMediationAdapter, true);
        }
    }

    @Override
    public void didClickInterstitial(String location) {
        if(interstitialMediationListener != null) {
            interstitialMediationListener.onAdClicked(interstitialMediationAdapter);
        }
    }

    @Override
    public void didDisplayInterstitial(String location) {
        if(interstitialMediationListener != null) {
            interstitialMediationListener.onAdShowing(interstitialMediationAdapter);
        }
    }

    @Override
    public void didCacheRewardedVideo(String location) {
        super.didCacheRewardedVideo(location);
        Log.d(BuildConfig.LOG_TAG, "ChartBoost rewarded ad loaded");
        if(rewardedMediationListener != null) {
            rewardedMediationListener.onAdLoaded(rewardedAdMediationAdapter);
        }
    }

    @Override
    public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
        super.didFailToLoadRewardedVideo(location, error);
        Log.d(BuildConfig.LOG_TAG, "ChartBoost rewarded ad failed to load");
        if(rewardedMediationListener != null) {
            AdRequestResult adStatus;
            switch(error) {
                case INTERNAL:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case INTERNET_UNAVAILABLE:
                    adStatus = AdRequestResult.Network;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case TOO_MANY_CONNECTIONS:
                    adStatus = AdRequestResult.Network;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case WRONG_ORIENTATION:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case FIRST_SESSION_INTERSTITIALS_DISABLED:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case NETWORK_FAILURE:
                    adStatus = AdRequestResult.Network;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case NO_AD_FOUND:
                    adStatus = AdRequestResult.NoFill;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case SESSION_NOT_STARTED:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case IMPRESSION_ALREADY_VISIBLE:
                    rewardedMediationListener.onAdFailedToShow(rewardedAdMediationAdapter, AdClosedResult.EXPIRED);
                    break;
                case NO_HOST_ACTIVITY:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case USER_CANCELLATION:
                    rewardedMediationListener.onAdFailedToShow(rewardedAdMediationAdapter, AdClosedResult.ERROR);
                    break;
                case INVALID_LOCATION:
                    adStatus = AdRequestResult.Configuration;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case VIDEO_UNAVAILABLE:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case VIDEO_ID_MISSING:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_PLAYING_VIDEO:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case INVALID_RESPONSE:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case ASSETS_DOWNLOAD_FAILURE:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_CREATING_VIEW:
                    rewardedMediationListener.onAdFailedToShow(rewardedAdMediationAdapter, AdClosedResult.ERROR);
                    break;
                case ERROR_DISPLAYING_VIEW:
                    rewardedMediationListener.onAdFailedToShow(rewardedAdMediationAdapter, AdClosedResult.ERROR);
                    break;
                case INCOMPATIBLE_API_VERSION:
                    adStatus = AdRequestResult.Configuration;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case ERROR_LOADING_WEB_VIEW:
                    rewardedMediationListener.onAdFailedToShow(rewardedAdMediationAdapter, AdClosedResult.ERROR);
                    break;
                case ASSET_PREFETCH_IN_PROGRESS:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case EMPTY_LOCAL_AD_LIST:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case ACTIVITY_MISSING_IN_MANIFEST:
                    adStatus = AdRequestResult.Configuration;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                case EMPTY_LOCAL_VIDEO_LIST:
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
                default :
                    adStatus = AdRequestResult.Error;
                    rewardedMediationListener.onAdFailedToLoad(rewardedAdMediationAdapter, adStatus, error.toString());
                    break;
            }
        }
    }

    @Override
    public void didDismissRewardedVideo(String location) {
        super.didDismissRewardedVideo(location);
        Log.d(BuildConfig.LOG_TAG, "Dismissed chartboost rewarded video");
        if(rewardedMediationListener != null) {
            rewardedMediationListener.onAdClosed(rewardedAdMediationAdapter, rewardedComplete);
        }
    }

    @Override
    public void didClickRewardedVideo(String location) {
        super.didClickRewardedVideo(location);
        Log.d(BuildConfig.LOG_TAG, "Clicked chartboost rewarded video");
        if(rewardedMediationListener != null) {
            rewardedMediationListener.onAdClicked(rewardedAdMediationAdapter);
        }
    }

    @Override
    public void didCompleteRewardedVideo(String location, int reward) {
        super.didCompleteRewardedVideo(location, reward);
        Log.d(BuildConfig.LOG_TAG, "Completed chartboost rewarded video");
        rewardedComplete = true;
    }

    @Override
    public void didDisplayRewardedVideo(String location) {
        super.didDisplayRewardedVideo(location);
        Log.d(BuildConfig.LOG_TAG, "Displayed chartboost rewarded video");
        rewardedComplete = false;
        if(rewardedMediationListener != null) {
            rewardedMediationListener.onAdShowing(rewardedAdMediationAdapter);
        }
    }
}
