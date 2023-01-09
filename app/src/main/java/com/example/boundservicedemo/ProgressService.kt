package com.example.boundservicedemo

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class ProgressService : Service() {

    private val binder = ProgressBinder()
    private var mHandler = Handler(Looper.getMainLooper())
    var mProgress: Int = 0
    var mMaxValue: Int = 15000
    var mIsPaused: Boolean = true

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: called.")
        mProgress = 0
        mIsPaused = true
        mMaxValue = 15000
    }

    override fun onBind(p0: Intent?): IBinder {
        Log.d(TAG, "onBind: called.")
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: called.")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called.")
    }

    fun pausePretendLongRunningTask() {
        mIsPaused = true
    }

    fun unPausePretendLongRunningTask() {
        mIsPaused = false
        startPretendLongRunningTask()
    }

    fun resetTask() {
        mProgress = 0
    }

    private fun startPretendLongRunningTask() {
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (mProgress >= mMaxValue || mIsPaused) {
                    Log.d(TAG, "service: run removing callbacks")
                    mHandler.removeCallbacks(this) // remove callbacks from runnable
                    pausePretendLongRunningTask()
                } else {
                    Log.d(TAG, "service: run progress")
                    mProgress += 100 // increment the progress
                    mHandler.postDelayed(this, 100) // continue incrementing
                }
            }
        }
        mHandler.postDelayed(runnable, 100)
    }

    class ProgressBinder : Binder() {
        fun getService(): ProgressService = instace
    }

    companion object {
        private const val TAG = "liviu"

        val instace: ProgressService by lazy {
            ProgressService()
        }
    }

}