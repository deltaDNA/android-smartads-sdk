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

package com.deltadna.android.sdk.ads.core;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.deltadna.android.sdk.ads.core.utils.Preconditions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

final class AdMetrics {
    
    private static final String DECISION_POINTS = "ddna_collected_decision_points";
    
    private enum Attr {
        LAST_SHOWN,
        SESSION_COUNT,
        DAILY_COUNT;
        
        final String keyFor(String decisionPoint) {
            return decisionPoint + '_' + name().toLowerCase(Locale.ENGLISH);
        }
    }
    
    private final SharedPreferences prefs;
    
    private boolean newDay;
    
    AdMetrics(SharedPreferences prefs) {
        this.prefs = prefs;
    }
    
    @Nullable
    Date lastShown(String decisionPoint) {
        validate(decisionPoint);
        
        final String key = Attr.LAST_SHOWN.keyFor(decisionPoint);
        return prefs.contains(key)
                ? new Date(prefs.getLong(key, 0))
                : null;
    }
    
    int sessionCount(String decisionPoint) {
        validate(decisionPoint);
        
        return prefs.getInt(Attr.SESSION_COUNT.keyFor(decisionPoint), 0);
    }
    
    int dailyCount(String decisionPoint) {
        validate(decisionPoint);
        
        return prefs.getInt(Attr.DAILY_COUNT.keyFor(decisionPoint), 0);
    }
    
    void recordAdShown(String decisionPoint, Date date) {
        validate(decisionPoint);
        
        final Set<String> decisionPoints = prefs.getStringSet(
                DECISION_POINTS, new HashSet<String>());
        decisionPoints.add(decisionPoint);
        
        int sessionCount = 1 + prefs.getInt(
                Attr.SESSION_COUNT.keyFor(decisionPoint), 0);
        int dayCount = 1 + prefs.getInt(
                Attr.DAILY_COUNT.keyFor(decisionPoint), 0);
        
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        keepDateOnly(calendar);
        
        if (startedNewDay(
                new Date(prefs.getLong(
                        Attr.LAST_SHOWN.keyFor(decisionPoint),
                        new Date().getTime())),
                calendar.getTime())) {
            newDay = true;
        }
        
        prefs   .edit()
                .putInt(Attr.SESSION_COUNT.keyFor(decisionPoint), sessionCount)
                .putInt(Attr.DAILY_COUNT.keyFor(decisionPoint), dayCount)
                .putLong(Attr.LAST_SHOWN.keyFor(decisionPoint), date.getTime())
                .putStringSet(DECISION_POINTS, decisionPoints)
                .apply();
    }
    
    void newSession(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        keepDateOnly(calendar);
        
        for (   final String decisionPoint
                : prefs.getStringSet(DECISION_POINTS, new HashSet<String>())) {
            
            final boolean resetDailyCount =
                    startedNewDay(
                            new Date(prefs.getLong(
                                    Attr.LAST_SHOWN.keyFor(decisionPoint), 0)),
                            calendar.getTime())
                    || newDay;
            
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Attr.SESSION_COUNT.keyFor(decisionPoint), 0);
            if (resetDailyCount) {
                editor.putInt(Attr.DAILY_COUNT.keyFor(decisionPoint), 0);
            }
            editor.apply();
        }
        
        newDay = false;
    }
    
    private static void validate(String decisionPoint) {
        Preconditions.checkArg(
                !TextUtils.isEmpty(decisionPoint),
                "Decision point cannot be null empty");
    }
    
    private static boolean startedNewDay(Date last, Date current) {
        final Calendar lastCal = Calendar.getInstance();
        final Calendar currentCal = Calendar.getInstance();
        
        lastCal.setTime(last);
        currentCal.setTime(current);
        
        keepDateOnly(lastCal);
        keepDateOnly(currentCal);
        
        return current.after(last);
    }
    
    private static void keepDateOnly(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
