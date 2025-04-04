/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.utils;

import java.util.Date;

public interface ICalibratedTime {

    /**
     * Get the time after calibration
     *
     * @param elapsedRealtime System Up Time
     * @return Date
     */
    Date get(long elapsedRealtime);
}
