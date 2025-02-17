/*
 * Copyright (C) 2022 ThinkingData
 */

package ivy.data.analytics.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Encapsulation time class
 * */
public class TDTime implements ITime {

    private final TimeZone mTimeZone;

    private final Date mDate;

    private boolean enableZoneOffset = true;

    public boolean mCalibrationDisuse= false;

    public TDTime(Date date, TimeZone timeZone) {
        mDate = date == null ? new Date() : date;
        mTimeZone = timeZone;

    }

    /**
     * getTime
     *
     * @return {@link String}
     */
    public String getTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(TDConstants.TIME_PATTERN, Locale.CHINA);
            if (null != mTimeZone) {
                dateFormat.setTimeZone(mTimeZone);
            }
            String ret = dateFormat.format(mDate);
//            String ret = TimeUtil.formatDate(mDate, TDConstants.TIME_PATTERN, mTimeZone);
            if (!Pattern.compile(TDConstants.TIME_CHECK_PATTERN).matcher(ret).find()) {
                ret = TDUtils.formatTime(mDate, mTimeZone);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void disableZoneOffset() {
        enableZoneOffset = false;
    }

    /**
     * getZoneOffset
     *
     * @return {@link Double}
     */
    public Double getZoneOffset() {
        if (enableZoneOffset && mTimeZone != null) {
            return TDUtils.getTimezoneOffset(mDate.getTime(), mTimeZone);
        }
        return null;
    }

    @Override
    public long getTimeMilliseconds() {
        return mDate.getTime();
    }

    @Override
    public String getUTCTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(TDConstants.TIME_PATTERN, Locale.CHINA);
            TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
            dateFormat.setTimeZone(utcTimeZone);
            String ret = dateFormat.format(mDate);
//            String ret = TimeUtil.formatDate(mDate, TDConstants.TIME_PATTERN, mTimeZone);
            if (!Pattern.compile(TDConstants.TIME_CHECK_PATTERN).matcher(ret).find()) {
                ret = TDUtils.formatTime(mDate, utcTimeZone);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCalibrationDisuse(boolean isDisuse){
        this.mCalibrationDisuse = isDisuse;
    }

}
