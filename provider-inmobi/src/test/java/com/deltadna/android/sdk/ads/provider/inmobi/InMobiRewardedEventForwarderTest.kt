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

package com.deltadna.android.sdk.ads.provider.inmobi

import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiInterstitial
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InMobiRewardedEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    private val ad = mock<InMobiRewardedEventForwarder>()
    
    private var uut = InMobiRewardedEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = InMobiRewardedEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter, ad)
    }
    
    @Test
    fun onAdReceived() {
        uut.onAdReceived(mock())
        
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun onAdLoadFailed() {
        val ad = mock<InMobiInterstitial>()
        uut.onAdReceived(ad)
        uut.onAdLoadFailed(
                ad,
                with(InMobiAdRequestStatus(InMobiAdRequestStatus.StatusCode.NO_FILL)) {
                    setCustomMessage("message")
                    this
                })
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                argThat { contains("message") })
    }
    
    @Test
    fun onAdLoadSucceeded() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
        }
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun onAdWillDisplay() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
        }
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun onAdDisplayed() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayed(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
        }
    }
    
    @Test
    fun onAdDisplayFailed() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayFailed(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdFailedToShow(
                    same(adapter),
                    eq(AdClosedResult.ERROR))
        }
    }
    
    @Test
    fun onAdInteraction() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayed(this)
            uut.onAdInteraction(this, mock())
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClicked(same(adapter))
        }
    }
    
    @Test
    fun onAdDismissedWithoutReward() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayed(this)
            uut.onAdDismissed(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun onAdDismissedWithReward() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayed(this)
            uut.onAdRewardActionCompleted(this, mock())
            uut.onAdDismissed(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun onUserLeftApplication() {
        with(mock<InMobiInterstitial>()) {
            uut.onAdReceived(this)
            uut.onAdLoadSucceeded(this)
            uut.onAdWillDisplay(this)
            uut.onAdDisplayed(this)
            uut.onUserLeftApplication(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdLeftApplication(same(adapter))
        }
    }
    
    private fun inOrder(vararg mocks: Any, block: org.mockito.InOrder.() -> Unit) {
        block.invoke(org.mockito.Mockito.inOrder(*mocks))
    }
}
