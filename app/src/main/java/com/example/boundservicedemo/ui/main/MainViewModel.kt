package com.example.boundservicedemo.ui.main

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.boundservicedemo.ProgressService

class MainViewModel : ViewModel() {

    private val mIsProgressBarUpdating: MutableLiveData<Boolean> = MutableLiveData()
    val mBinder: MutableLiveData<ProgressService.ProgressBinder?> = MutableLiveData()


    // Keeping this in here because it doesn't require a context
    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.")
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            val binder: ProgressService.ProgressBinder = iBinder as ProgressService.ProgressBinder
            mBinder.postValue(binder)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "ServiceConnection: disconnected from service.")
            mBinder.postValue(null)
        }
    }

    fun getIsProgressBarUpdating(): LiveData<Boolean?> = mIsProgressBarUpdating

    fun setIsProgressBarUpdating(isUpdating: Boolean) {
        mIsProgressBarUpdating.postValue(isUpdating)
    }

    companion object {
        private const val TAG = "liviu"
    }
}