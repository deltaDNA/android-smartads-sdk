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

package com.deltadna.android.sdk.ads.provider.unity

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import com.unity3d.ads.UnityAds
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class EventForwarderTest {
    
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = EventForwarder(adapter, listener)
    
    @Before
    fun before() {
        uut = EventForwarder(adapter, listener)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun loaded() {
        uut.requestPerformed(listener, "")
        // simulate Unity calling ready in quick succession
        uut.onUnityAdsReady("")
        uut.onUnityAdsReady("")
        advance()
        
        // but we only want a single callback invocation
        verify(listener).onAdLoaded(adapter)
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun failedToLoad() {
        uut.requestPerformed(listener, "")
        uut.onUnityAdsError(UnityAds.UnityAdsError.INTERNAL_ERROR, "message")
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Error),
                eq("message"))
    }
    
    @Test
    fun fullCycleCompleted() {
        uut.requestPerformed(listener, "")
        uut.onUnityAdsReady("")
        advance()
        uut.onUnityAdsStart("")
        uut.onUnityAdsFinish("", UnityAds.FinishState.COMPLETED)
        // simulate Unity calling ready after a cycle
        uut.onUnityAdsReady("")
        advance()
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun fullCycleNotComplete() {
        uut.requestPerformed(listener, "")
        uut.onUnityAdsFinish("", UnityAds.FinishState.SKIPPED)
        
        verify(listener).onAdClosed(same(adapter), eq(false))
    }
    
    @Test
    fun loadedCalledOnNextCycle() {
        uut.requestPerformed(listener, "")
        uut.onUnityAdsReady("")
        advance()
        uut.onUnityAdsFinish("", UnityAds.FinishState.COMPLETED)
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener, "")
        advance()
        
        inOrder(listener, nextListener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
            verify(nextListener).onAdLoaded(same(adapter))
        }
        verifyNoMoreInteractions(listener)
        verifyNoMoreInteractions(nextListener)
    }
    
    @Test
    fun failureCarriedOn() {
        uut.requestPerformed(listener, "")
        uut.onUnityAdsError(UnityAds.UnityAdsError.INTERNAL_ERROR, "message")
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener, "")
        advance()
        
        inOrder(listener, nextListener) {
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.Error),
                    eq("message"))
            verify(nextListener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.Error),
                    eq("message"))
        }
    }
    
    private fun advance() {
        RuntimeEnvironment
                .getMasterScheduler()
                .advanceBy(1500, TimeUnit.MILLISECONDS)
    }
}