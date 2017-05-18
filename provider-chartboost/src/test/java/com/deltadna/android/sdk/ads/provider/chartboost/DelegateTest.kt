/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.chartboost

import com.chartboost.sdk.Model.CBError
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DelegateTest {
    
    private val interstitialAdapter = mock<MediationAdapter>()
    private val interstitialListener = mock<MediationListener>()
    private val rewardedAdapter = mock<MediationAdapter>()
    private val rewardedListener = mock<MediationListener>()
    
    private var uut = Delegate()
    
    @Before
    fun before() {
        uut = Delegate()
    }
    
    @After
    fun after() {
        reset(  interstitialAdapter,
                interstitialListener,
                rewardedAdapter,
                rewardedListener)
    }
    
    @Test
    fun listenersUnset() {
        uut.setInterstitial(interstitialListener, interstitialAdapter)
        uut.setRewarded(rewardedListener, rewardedAdapter)
        uut.setInterstitial(null, null)
        uut.setRewarded(null, null)
        
        uut.didCacheInterstitial("")
        uut.didFailToLoadInterstitial("", CBError.CBImpressionError.NO_AD_FOUND)
        uut.didDisplayInterstitial("")
        uut.didClickInterstitial("")
        uut.didCloseInterstitial("")
        
        uut.didCacheRewardedVideo("")
        uut.didFailToLoadRewardedVideo("", CBError.CBImpressionError.NO_AD_FOUND)
        uut.didDisplayRewardedVideo("")
        uut.didClickRewardedVideo("")
        uut.didCompleteRewardedVideo("", 0)
        uut.didCloseRewardedVideo("")
        
        verifyZeroInteractions(interstitialListener)
        verifyZeroInteractions(rewardedListener)
    }
    
    @Test
    fun interstitial() {
        uut.setInterstitial(interstitialListener, interstitialAdapter)
        
        uut.didCacheInterstitial("")
        uut.didDisplayInterstitial("")
        uut.didClickInterstitial("")
        uut.didCloseInterstitial("")
        
        inOrder(interstitialListener) {
            verify(interstitialListener).onAdLoaded(same(interstitialAdapter))
            verify(interstitialListener).onAdShowing(same(interstitialAdapter))
            verify(interstitialListener).onAdClicked(same(interstitialAdapter))
            verify(interstitialListener).onAdClosed(
                    same(interstitialAdapter),
                    eq(true))
        }
    }
    
    @Test
    fun interstitialFailed() {
        uut.setInterstitial(interstitialListener, interstitialAdapter)
        uut.didFailToLoadInterstitial("", CBError.CBImpressionError.NO_AD_FOUND)
        
        verify(interstitialListener).onAdFailedToLoad(
                same(interstitialAdapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun interstitialLoadedCalledTwice() {
        uut.setInterstitial(interstitialListener, interstitialAdapter)
        
        uut.didCacheInterstitial("")
        uut.didDisplayInterstitial("")
        uut.didCacheInterstitial("")
        
        verify(interstitialListener).onAdLoaded(same(interstitialAdapter))
        verify(interstitialListener).onAdShowing(same(interstitialAdapter))
        verifyNoMoreInteractions(interstitialAdapter)
    }
    
    @Test
    fun interstitialFailedToShow() {
        uut.setInterstitial(interstitialListener, interstitialAdapter)
        
        uut.didCacheInterstitial("")
        uut.didFailToLoadInterstitial("", CBError.CBImpressionError.NO_HOST_ACTIVITY)
        
        verify(interstitialListener).onAdLoaded(same(interstitialAdapter))
        verify(interstitialListener).onAdFailedToShow(
                same(interstitialAdapter),
                eq(AdClosedResult.ERROR))
    }
    
    @Test
    fun rewardedWithoutReward() {
        uut.setRewarded(rewardedListener, rewardedAdapter)
        
        uut.didCacheRewardedVideo("")
        uut.didDisplayRewardedVideo("")
        uut.didClickRewardedVideo("")
        uut.didCloseRewardedVideo("")
        
        inOrder(rewardedListener) {
            verify(rewardedListener).onAdLoaded(same(rewardedAdapter))
            verify(rewardedListener).onAdShowing(same(rewardedAdapter))
            verify(rewardedListener).onAdClicked(same(rewardedAdapter))
            verify(rewardedListener).onAdClosed(
                    same(rewardedAdapter),
                    eq(false))
        }
    }
    
    @Test
    fun rewardedWithReward() {
        uut.setRewarded(rewardedListener, rewardedAdapter)
        
        uut.didCacheRewardedVideo("")
        uut.didDisplayRewardedVideo("")
        uut.didClickRewardedVideo("")
        uut.didCompleteRewardedVideo("", 0)
        uut.didCloseRewardedVideo("")
        
        inOrder(rewardedListener) {
            verify(rewardedListener).onAdLoaded(same(rewardedAdapter))
            verify(rewardedListener).onAdShowing(same(rewardedAdapter))
            verify(rewardedListener).onAdClicked(same(rewardedAdapter))
            verify(rewardedListener).onAdClosed(
                    same(rewardedAdapter),
                    eq(true))
        }
    }
    
    @Test
    fun rewardedFailed() {
        uut.setRewarded(rewardedListener, rewardedAdapter)
        uut.didFailToLoadRewardedVideo("", CBError.CBImpressionError.NO_AD_FOUND)
        
        verify(rewardedListener).onAdFailedToLoad(
                same(rewardedAdapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun rewardedLoadedCalledTwice() {
        uut.setRewarded(rewardedListener, rewardedAdapter)
        
        uut.didCacheRewardedVideo("")
        uut.didDisplayRewardedVideo("")
        uut.didCacheRewardedVideo("")
        
        verify(rewardedListener).onAdLoaded(same(rewardedAdapter))
        verify(rewardedListener).onAdShowing(same(rewardedAdapter))
        verifyNoMoreInteractions(rewardedAdapter)
    }
    
    @Test
    fun rewardedFailedToShow() {
        uut.setRewarded(rewardedListener, rewardedAdapter)
        
        uut.didCacheRewardedVideo("")
        uut.didFailToLoadRewardedVideo("", CBError.CBImpressionError.NO_HOST_ACTIVITY)
        
        verify(rewardedListener).onAdLoaded(same(rewardedAdapter))
        verify(rewardedListener).onAdFailedToShow(
                same(rewardedAdapter),
                eq(AdClosedResult.ERROR))
    }
}
