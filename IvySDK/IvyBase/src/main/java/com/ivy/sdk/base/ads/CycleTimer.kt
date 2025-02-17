package com.ivy.sdk.base.ads

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock

abstract class CycleTimer {


    /**
     * Millis since epoch when alarm should stop.
     */
    private var mMillisInFuture: Long = 0

    /**
     * The interval in millis that the user receives callbacks
     */
    private var mCountdownInterval: Long = 0

    /**
     * expected stop time
     */
    private var mStopTimeInFuture: Long = 0

    /**
     * boolean representing if the timer was cancelled
     */
    private var mCancelled = false

    /**
     * boolean representing if the timer was paused
     */
    private var mPaused = false

    /**
     * @param millisInFuture The number of millis in the future from the call
     * to [.start] until the countdown is done and [.onFinish]
     * is called.
     * @param countDownInterval The interval along the way to receive
     * [.onTick] callbacks.
     */
    constructor(millisInFuture: Long, countDownInterval: Long) {
        mMillisInFuture = millisInFuture
        mCountdownInterval = countDownInterval
    }

    /**
     * Cancel the countdown.
     */
    @Synchronized
    fun cancel() {
        mCancelled = true
        mHandler.removeMessages(MSG)
    }

    /**
     * Start the countdown.
     */
    @Synchronized
    fun start(): CycleTimer {
        mCancelled = false
        if (mMillisInFuture <= 0) {
            onSection()
            return this
        }
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture
        mHandler.sendMessage(mHandler.obtainMessage(MSG))
        return this
    }

    @Synchronized
    fun resume() {
        mPaused = false
    }

    @Synchronized
    fun pause() {
        mPaused = true
    }

    @Synchronized
    fun reset(){
        mHandler.removeMessages(MSG)
        mCancelled = false
        if (mMillisInFuture <= 0) {
            onSection()
            return
        }
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture
        mHandler.sendMessage(mHandler.obtainMessage(MSG))
    }

    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    abstract fun onTick(millisUntilFinished: Long)

    /**
     * Callback fired when the time is up.
     */
    abstract fun onSection()


    companion object {
        val MSG: Int = 1
    }

    // handles counting down
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            synchronized(this@CycleTimer) {
                if (mCancelled) {
                    return
                }
                if (mPaused) {
                    mStopTimeInFuture += SystemClock.elapsedRealtime()
                    sendMessageDelayed(obtainMessage(MSG), 500)
                    return
                }
                val millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime()
                if (millisLeft <= 0) {
                    onSection()
                    mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture
                    sendMessageDelayed(obtainMessage(MSG), mCountdownInterval)
                } else {
                    val lastTickStart = SystemClock.elapsedRealtime()
                    onTick(millisLeft)

                    // take into account user's onTick taking time to execute
                    val lastTickDuration =
                        SystemClock.elapsedRealtime() - lastTickStart
                    var delay: Long

                    if (millisLeft < mCountdownInterval) {
                        // just delay until done
                        delay = millisLeft - lastTickDuration

                        // special case: user's onTick took more than interval to
                        // complete, trigger onFinish without delay
                        if (delay < 0) delay = 0
                    } else {
                        delay = mCountdownInterval - lastTickDuration

                        // special case: user's onTick took more than interval to
                        // complete, skip to next interval
                        while (delay < 0) delay += mCountdownInterval
                    }

                    sendMessageDelayed(obtainMessage(MSG), delay)
                }
            }
        }
    }

}