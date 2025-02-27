/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.persistence;

import android.content.SharedPreferences;
import java.util.UUID;
import java.util.concurrent.Future;

import cn.thinkingdata.core.sp.SharedPreferencesStorage;

/**
 * StorageRandomID.
 * */
public class StorageRandomID extends SharedPreferencesStorage<String> {
    public StorageRandomID(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, "randomID");
    }

    @Override
    protected String create() {
        return UUID.randomUUID().toString();
    }
}