/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.utils;

/**
 * Exceptions are generated in Debug mode.
 * */
public class TDDebugException extends RuntimeException {
    public TDDebugException(String message) {
        super(message);
    }

    public TDDebugException(Throwable cause) {
        super(cause);
    }
}
