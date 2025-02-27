/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.utils;

import java.io.IOException;
import java.util.Map;

import ivy.data.analytics.TDConfig;

/**
 * RemoteService.
 * */
public interface RemoteService {
    String performRequest(TDConfig config, String params,
                          final Map<String, String> extraHeaders)
            throws IOException, ServiceUnavailableException;

    /**
     * ServiceUnavailableException.
     * */
    class ServiceUnavailableException extends Exception {
        ServiceUnavailableException(String message) {
            super(message);
        }
    }
}
