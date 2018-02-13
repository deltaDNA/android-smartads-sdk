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
import com.google.common.truth.Truth.*
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DynamicActivityCatcherTest {
    
    @Test
    fun activityCaptured() {
        with(DynamicActivityCatcher(mock())) {
            val activity1 = mock<Activity>()
            val activity2 = mock<Activity>()
            val activity3 = mock<Activity>()
            
            onActivityCreated(activity1, mock())
            onActivityCreated(activity2, mock())
            onActivityCreated(activity3, mock())
            assertThat(getActivity()).isNull()
            onActivityDestroyed(activity3)
            assertThat(getActivity()).isNull()
            
            onAnalyticsStarted()
            assertThat(getActivity()).isSameAs(activity2)
        }
    }
    
    @Test
    fun activityReleased() {
        with(DynamicActivityCatcher(mock())) {
            val activity = mock<Activity>()
            onActivityCreated(activity, mock())
            onAnalyticsStarted()
            
            onAnalyticsStopped()
            assertThat(getActivity()).isNull()
        }
    }
    
    @Test
    fun resumeAndPauseCallbacks() {
        val listener = mock<ActivityCatcher.LifecycleCallbacks>()
        with(DynamicActivityCatcher(listener)) {
            val activity = mock<Activity>()
            onActivityCreated(activity, mock())
            onAnalyticsStarted()
            
            onActivityResumed(activity)
            verify(listener).onResumed()
            
            onActivityPaused(activity)
            verify(listener).onPaused()
        }
    }
    
    @Test
    fun ignoresOtherActivities() {
        val listener = mock<ActivityCatcher.LifecycleCallbacks>()
        with(DynamicActivityCatcher(listener)) {
            val activity1 = mock<Activity>()
            val activity2 = mock<Activity>()
            onActivityCreated(activity1, mock())
            onAnalyticsStarted()
            onActivityCreated(activity2, mock())
            
            onActivityResumed(activity2)
            onActivityPaused(activity2)
            verifyZeroInteractions(listener)
            
            onActivityDestroyed(activity2)
            assertThat(getActivity()).isNotNull()
        }
    }
}
