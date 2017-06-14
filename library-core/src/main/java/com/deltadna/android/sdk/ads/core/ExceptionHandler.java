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

package com.deltadna.android.sdk.ads.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    
    @Nullable
    private final Thread.UncaughtExceptionHandler delegate =
            Thread.getDefaultUncaughtExceptionHandler();
    
    private final DbHelper db;
    
    private final String version;
    private final int versionCode;
    
    private final String versionSdk;
    private final String versionSmartAdsSdk;
    
    ExceptionHandler(
            Context context,
            String version,
            int versionCode,
            String versionSdk,
            String versionSmartAdsSdk) {
        
        db = new DbHelper(context);
        
        this.version = version;
        this.versionCode = versionCode;
        
        this.versionSdk = versionSdk;
        this.versionSmartAdsSdk = versionSmartAdsSdk;
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        for (StackTraceElement element : e.getStackTrace()) {
            for (AdProvider provider : AdProvider.values()) {
                if (    element.getClassName().equals(provider.cls) ||
                        element.getClassName().startsWith(provider.namespace)) {
                    
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    
                    db.add( new Date().getTime(),
                            provider.name(),
                            version,
                            versionCode,
                            versionSdk,
                            versionSmartAdsSdk,
                            provider.version(),
                            sw.toString());
                }
            }
        }
        
        if (delegate != null) delegate.uncaughtException(t, e);
    }
    
    List<String> listCrashes(AdProvider provider) {
        final Cursor cursor = db.list(
                provider.name(),
                version,
                versionCode,
                versionSdk,
                versionSmartAdsSdk,
                provider.version());
        final List<String> crashes = new ArrayList<>(cursor.getCount());
        
        while (cursor.moveToNext()) {
            crashes.add(cursor.getString(cursor.getColumnIndex(
                    DbHelper.CRASHES_STACK_TRACE)));
        }
        cursor.close();
        
        return crashes;
    }
    
    private static final class DbHelper extends SQLiteOpenHelper {
        
        private static final String TABLE_CRASHES = "Crashes";
        
        private static final String CRASHES_ID = BaseColumns._ID;
        private static final String CRASHES_TIME = "Time";
        private static final String CRASHES_ADAPTER = "Adapter";
        private static final String CRASHES_VERSION = "Version";
        private static final String CRASHES_VERSION_CODE = "VersionCode";
        private static final String CRASHES_VERSION_SDK = "VersionSdk";
        private static final String CRASHES_VERSION_SMARTADS_SDK = "VersionSmartAdsSdk";
        private static final String CRASHES_VERSION_NETWORK = "VersionNetwork";
        private static final String CRASHES_STACK_TRACE = "StackTrace";
        
        DbHelper(Context context) {
            super(context, "com.deltadna.android.sdk.ads", null, 1);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CRASHES + "("
                    + CRASHES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CRASHES_TIME + " INTEGER NOT NULL, "
                    + CRASHES_ADAPTER + " TEXT NOT NULL, "
                    + CRASHES_VERSION + " TEXT NOT NULL, "
                    + CRASHES_VERSION_CODE + " INTEGER NOT NULL, "
                    + CRASHES_VERSION_SDK + " TEXT NOT NULL, "
                    + CRASHES_VERSION_SMARTADS_SDK + " TEXT NOT NULL, "
                    + CRASHES_VERSION_NETWORK + " TEXT NOT NULL, "
                    + CRASHES_STACK_TRACE + " TEXT NOT NULL)");
        }
        
        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {}
        
        boolean add(
                long time,
                String adapter,
                String version,
                int versionCode,
                String versionSdk,
                String versionSmartAdsSdk,
                String versionNetwork,
                String stackTrace) {
            
            final ContentValues values = new ContentValues(8);
            values.put(CRASHES_TIME, time);
            values.put(CRASHES_ADAPTER, adapter);
            values.put(CRASHES_VERSION, version);
            values.put(CRASHES_VERSION_CODE, versionCode);
            values.put(CRASHES_VERSION_SDK, versionSdk);
            values.put(CRASHES_VERSION_SMARTADS_SDK, versionSmartAdsSdk);
            values.put(CRASHES_VERSION_NETWORK, versionNetwork);
            values.put(CRASHES_STACK_TRACE, stackTrace);
            
            return (getWritableDatabase().insert(TABLE_CRASHES, null, values)
                    != -1);
        }
        
        Cursor list(
                String adapter,
                String version,
                int versionCode,
                String versionSdk,
                String versionSmartAdsSdk,
                String versionNetwork) {
            
            return getReadableDatabase().query(
                    TABLE_CRASHES,
                    new String[] {CRASHES_STACK_TRACE},
                    String.format(
                            Locale.US,
                            "%s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?",
                            CRASHES_ADAPTER,
                            CRASHES_VERSION,
                            CRASHES_VERSION_CODE,
                            CRASHES_VERSION_SDK,
                            CRASHES_VERSION_SMARTADS_SDK,
                            CRASHES_VERSION_NETWORK),
                    new String[] {
                            adapter,
                            version,
                            Integer.toString(versionCode),
                            versionSdk,
                            versionSmartAdsSdk,
                            versionNetwork},
                    null,
                    null,
                    CRASHES_TIME + " DESC",
                    null);
        }
    }
}
