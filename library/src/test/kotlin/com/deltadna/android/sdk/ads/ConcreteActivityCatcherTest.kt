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

package com.deltadna.android.sdk.ads

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConcreteActivityCatcherTest {
    
    @Test
    fun activityCaptured() {
        with(ConcreteActivityCatcher(mock(), TestActivity::class.java)) {
            assertThat(getActivity()).isNull()
            
            val activity = TestActivity()
            onActivityCreated(activity, mock())
            
            assertThat(getActivity()).isSameAs(activity)
        }
    }
    
    @Test
    fun activityReleased() {
        with(ConcreteActivityCatcher(mock(), TestActivity::class.java)) {
            val activity = TestActivity()
            onActivityCreated(activity, mock())
            onActivityDestroyed(activity)
            
            assertThat(getActivity()).isNull()
        }
    }
    
    @Test
    fun resumeAndPauseCallbacks() {
        val listener = mock<ActivityCatcher.LifecycleCallbacks>()
        with(ConcreteActivityCatcher(listener, TestActivity::class.java)) {
            val activity = TestActivity()
            onActivityCreated(activity, mock())
            
            onActivityResumed(activity)
            verify(listener).onResumed()
            
            onActivityPaused(activity)
            verify(listener).onPaused()
        }
    }
    
    @Test
    fun ignoresOtherActivities() {
        val listener = mock<ActivityCatcher.LifecycleCallbacks>()
        with(ConcreteActivityCatcher(listener, TestActivity::class.java)) {
            val activity = mock<Activity>()
            onActivityCreated(activity, mock())
            assertThat(getActivity()).isNull()
            
            onActivityCreated(TestActivity(), mock())
            assertThat(getActivity()).isNotNull()
            
            onActivityResumed(activity)
            onActivityPaused(activity)
            verifyZeroInteractions(listener)
            
            onActivityDestroyed(activity)
            assertThat(getActivity()).isNotNull()
        }
    }
    
    private class TestActivity : Activity()
}
