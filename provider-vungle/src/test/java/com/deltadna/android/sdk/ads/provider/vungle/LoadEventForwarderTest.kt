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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import com.vungle.warren.error.VungleException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LoadEventForwarderTest {
    
    private lateinit var adapter: MediationAdapter
    private lateinit var listener: MediationListener
    
    private lateinit var uut: LoadEventForwarder
    
    @Before
    fun before() {
        adapter = mock()
        listener = mock()
        
        uut = LoadEventForwarder(PLACEMENT_ID, adapter, listener)
    }
    
    @Test
    fun `on error with different placement does not notify listener`() {
        uut.onError("differentPlacementId", mock())
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun `on error notifies listener of failure to load due to no fill`() {
        uut.onError(PLACEMENT_ID, mock<VungleException>().apply {
            whenever(exceptionCode).then { VungleException.NO_SERVE }
            whenever(localizedMessage).then { "message" }
        })
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("message"))
    }
    
    @Test
    fun `on error notifies listener of failure to show due to error`() {
        uut.onError(PLACEMENT_ID, mock<VungleException>().apply {
            whenever(exceptionCode).then { VungleException.UNKNOWN_ERROR }
            whenever(localizedMessage).then { "message" }
        })
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Error),
                eq("message"))
    }
    
    @Test
    fun `on ad load with different placement does not notify listener`() {
        uut.onAdLoad("differentPlacementId")
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun `on ad load notifies listener of ad loaded`() {
        uut.onAdLoad(PLACEMENT_ID)
        verify(listener).onAdLoaded(same(adapter))
    }
    
    private companion object {
        
        const val PLACEMENT_ID = "placementId"
    }
}
