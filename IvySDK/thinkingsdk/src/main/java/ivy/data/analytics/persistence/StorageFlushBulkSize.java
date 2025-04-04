/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.persistence;

import android.content.SharedPreferences;
import java.util.concurrent.Future;

import cn.thinkingdata.core.sp.SharedPreferencesStorage;

/**
 * StorageFlushBulkSize.
 * */
public class StorageFlushBulkSize extends SharedPreferencesStorage<Integer> {
    private final int mDefaultBulkSize;

    public StorageFlushBulkSize(Future<SharedPreferences> loadStoredPreferences, int defaultBulkSize) {
        super(loadStoredPreferences, "flushBulkSize");
        mDefaultBulkSize = defaultBulkSize;
    }

    @Override
    protected void save(SharedPreferences.Editor editor, Integer interval) {
        editor.putInt(storageKey, interval);
        editor.apply();
    }

    protected void load(SharedPreferences sharedPreferences) {
        data = sharedPreferences.getInt(this.storageKey, mDefaultBulkSize);
    }
}
