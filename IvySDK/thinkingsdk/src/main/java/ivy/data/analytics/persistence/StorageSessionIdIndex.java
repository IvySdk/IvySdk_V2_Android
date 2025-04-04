/*
 * Copyright (C) 2022 ThinkingData
 */
package ivy.data.analytics.persistence;

import android.content.SharedPreferences;

import java.util.concurrent.Future;

import cn.thinkingdata.core.sp.SharedPreferencesStorage;

/**
 * session index storage
 *
 * @author liulongbing
 * @since 2022/11/29
 */
public class StorageSessionIdIndex extends SharedPreferencesStorage<Integer> {

    StorageSessionIdIndex(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, "sessionId");
    }

    @Override
    protected void save(SharedPreferences.Editor editor, Integer data) {
        editor.putInt(storageKey, data);
        editor.apply();
    }

    @Override
    protected void load(SharedPreferences sharedPreferences) {
        data = sharedPreferences.getInt(this.storageKey, 0);
    }
}
