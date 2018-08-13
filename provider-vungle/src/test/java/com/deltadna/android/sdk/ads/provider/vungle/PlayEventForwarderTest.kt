/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.vungle

import com.deltadna.android.sdk.ads.bindings.AdShowResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import com.vungle.warren.error.VungleException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PlayEventForwarderTest {
    
    private lateinit var adapter: MediationAdapter
    private lateinit var listener: MediationListener
    
    private lateinit var uut: PlayEventForwarder
    
    @Before
    fun before() {
        adapter = mock()
        listener = mock()
        
        uut = PlayEventForwarder(PLACEMENT_ID, adapter, listener)
    }
    
    @Test
    fun `on error with different placement does not notify listener`() {
        uut.onError("differentPlacementId", mock())
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun `on error notifies listener of failure to show due to expiry`() {
        uut.onError(PLACEMENT_ID, VungleException(VungleException.AD_EXPIRED))
        verify(listener).onAdFailedToShow(same(adapter), eq(AdShowResult.EXPIRED))
    }
    
    @Test
    fun `on error notifies listener of failure to show due to error`() {
        uut.onError(PLACEMENT_ID, VungleException(VungleException.UNKNOWN_ERROR))
        verify(listener).onAdFailedToShow(same(adapter), eq(AdShowResult.ERROR))
    }
    
    @Test
    fun `on ad start with different placement does not notify listener`() {
        uut.onAdStart("differentPlacementId")
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun `on ad start notifies listener of ad showing`() {
        uut.onAdStart(PLACEMENT_ID)
        verify(listener).onAdShowing(same(adapter))
    }
    
    @Test
    fun `on ad end with different placement does not notify listener`() {
        uut.onAdEnd("differentPlacementId", true, true)
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun `on ad end without completion notifies listener of ad closure`() {
        uut.onAdEnd(PLACEMENT_ID, false, false)
        
        verify(listener).onAdClosed(same(adapter), eq(false))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun `on ad end with completion notifies listener of ad closure`() {
        uut.onAdEnd(PLACEMENT_ID, true, false)
        
        verify(listener).onAdClosed(same(adapter), eq(true))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun `on ad end with click notifies listener of ad click and closure`() {
        uut.onAdEnd(PLACEMENT_ID, true, true)
        
        inOrder(listener) {
            verify(listener).onAdClicked(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    private companion object {
        
        const val PLACEMENT_ID = "placementId"
    }
}
