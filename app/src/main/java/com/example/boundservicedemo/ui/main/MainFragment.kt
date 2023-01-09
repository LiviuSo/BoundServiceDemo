package com.example.boundservicedemo.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.boundservicedemo.ProgressService
import com.example.boundservicedemo.R

class MainFragment : Fragment() {


    // UI Components
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mTextView: TextView
    private lateinit var mButton: Button


    // Vars
    private lateinit var mService: ProgressService
    private lateinit var mViewModel: MainViewModel


    companion object {
        private const val TAG = "liviu"

        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_main, container, false).apply {
        mProgressBar = findViewById(R.id.progresss_bar)
        mTextView = findViewById(R.id.text_view)
        mButton = findViewById<Button?>(R.id.toggle_updates).apply {
            setOnClickListener { toggleUpdates() }
        }
        setObservers()
    }

    private fun toggleUpdates() {
        if (mService.mProgress == mService.mMaxValue) {
            mService.resetTask()
            mButton.text = "Start"
            mProgressBar.progress = 0
        } else {
            if (mService.mIsPaused) {
                mService.unPausePretendLongRunningTask()
                mViewModel.setIsProgressBarUpdating(true)
            } else {
                mService.pausePretendLongRunningTask()
                mViewModel.setIsProgressBarUpdating(false)
            }
        }
    }

    private fun setObservers() {
        mViewModel.mBinder.observe(requireActivity()) { myBinder ->
            if (myBinder == null) {
                Log.d(TAG, "onChanged: unbound from service")
            } else {
                Log.d(TAG, "onChanged: bound to service.")
                mService = myBinder.getService()
            }
        }

        mViewModel.getIsProgressBarUpdating().observe(requireActivity()) { aBoolean ->
            val handler = Handler(Looper.getMainLooper())
            val runnable: Runnable = object : Runnable {
                override fun run() {
                    if (mViewModel.getIsProgressBarUpdating().value == true) {
                        Log.d(TAG, "MainFragment: run progress")
                        if (mViewModel.mBinder.value != null) { // meaning the service is bound
                            if (mService.mProgress == mService.mMaxValue) {
                                mViewModel.setIsProgressBarUpdating(false)
                            }
                            mProgressBar.progress = mService.mProgress
                            mProgressBar.max = mService.mMaxValue
                            val progress: String =
                                java.lang.String.valueOf(100 * mService.mProgress / mService.mMaxValue) + "%"
                            mTextView.text = progress
                        }
                        handler.postDelayed(this, 100)
                    } else {
                        Log.d(TAG, "MainFragment: run removing callbacks")
                        handler.removeCallbacks(this)
                    }
                }
            }

            // control what the button shows
            if (aBoolean == true) {
                mButton.text = "Pause"
                handler.postDelayed(runnable, 100)
            } else {
                if (mService.mProgress == mService.mMaxValue) {
                    mButton.text = "Restart"
                } else {
                    mButton.text = "Start"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(mViewModel.serviceConnection)
    }

    private fun startService() {
        val serviceIntent = Intent(requireContext(), ProgressService::class.java)
        requireContext().startService(serviceIntent)
        bindService()
    }

    private fun bindService() {
        val serviceBindIntent = Intent(requireContext(), ProgressService::class.java)
        requireContext().bindService(serviceBindIntent, mViewModel.serviceConnection, Context.BIND_AUTO_CREATE)
    }


}