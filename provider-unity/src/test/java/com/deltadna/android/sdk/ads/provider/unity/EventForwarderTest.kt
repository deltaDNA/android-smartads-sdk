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
import com.unity3d.ads.UnityAds.PlacementState.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventForwarderTest {
    
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = EventForwarder(adapter, PLACEMENT_ID, listener)
    
    @Before
    fun before() {
        uut = EventForwarder(adapter, PLACEMENT_ID, listener)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun loaded() {
        uut.requestPerformed(listener)
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, WAITING, READY)
        
        verify(listener).onAdLoaded(adapter)
    }
    
    @Test
    fun failedToLoad() {
        uut.requestPerformed(listener)
        uut.onUnityAdsError(UnityAds.UnityAdsError.INTERNAL_ERROR, "message")
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Error),
                eq("message"))
    }
    
    @Test
    fun fullCycleCompleted() {
        uut.requestPerformed(listener)
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, WAITING, READY)
        uut.onUnityAdsStart(PLACEMENT_ID)
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, READY, WAITING)
        uut.onUnityAdsFinish(PLACEMENT_ID, UnityAds.FinishState.COMPLETED)
        // simulate Unity calling ready after a cycle
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, WAITING, READY)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun fullCycleNotComplete() {
        uut.requestPerformed(listener)
        uut.onUnityAdsFinish(PLACEMENT_ID, UnityAds.FinishState.SKIPPED)
        
        verify(listener).onAdClosed(same(adapter), eq(false))
    }
    
    @Test
    fun loadedCalledOnNextCycle() {
        uut.requestPerformed(listener)
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, WAITING, READY)
        uut.onUnityAdsFinish(PLACEMENT_ID, UnityAds.FinishState.COMPLETED)
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener)
        
        inOrder(listener, nextListener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
            verify(nextListener).onAdLoaded(same(adapter))
        }
        verifyNoMoreInteractions(listener)
        verifyNoMoreInteractions(nextListener)
    }
    
    @Test
    fun placementStateCarriedOn() {
        uut.requestPerformed(listener)
        uut.onUnityAdsPlacementStateChanged(PLACEMENT_ID, NO_FILL, NO_FILL)
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener)
        
        inOrder(listener, nextListener) {
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    eq(NO_FILL.name))
            verify(nextListener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    eq(NO_FILL.name))
        }
    }
    
    @Test
    fun failureCarriedOn() {
        uut.requestPerformed(listener)
        uut.onUnityAdsError(UnityAds.UnityAdsError.INTERNAL_ERROR, "message")
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener)
        
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
    
    @Test
    fun differentPlacementId() {
        uut.requestPerformed(listener)
        uut.onUnityAdsPlacementStateChanged("differentId", WAITING, READY)
        uut.onUnityAdsStart("differentId")
        
        verifyZeroInteractions(listener)
    }
    
    private companion object {
        
        val PLACEMENT_ID = "placementId"
    }
}
