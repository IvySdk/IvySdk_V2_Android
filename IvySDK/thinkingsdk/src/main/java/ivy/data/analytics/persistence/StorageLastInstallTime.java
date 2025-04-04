/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.persistence;

import android.content.SharedPreferences;

import java.util.concurrent.Future;

import cn.thinkingdata.core.sp.SharedPreferencesStorage;

/**
 * StorageLastInstallTime.
 */
public class StorageLastInstallTime extends SharedPreferencesStorage<Long> {

    public StorageLastInstallTime(String prefix, Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, prefix + "_lastInstallTime");
    }

    @Override
    public Long create() {
        return 0L;
    }

    @Override
    public void save(SharedPreferences.Editor editor, Long installTime) {
        editor.putLong(this.storageKey, installTime);
        editor.apply();
    }

    @Override
    public void load(SharedPreferences sharedPreferences) {
        this.data = sharedPreferences.getLong(this.storageKey, 0L);
    }
}