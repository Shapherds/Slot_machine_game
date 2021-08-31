package com.cheerful.joker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.cheerful.joker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var pStatus = 0
    private lateinit var userInterface: ActivityMainBinding

    @SuppressLint("SourceLockedOrientationActivity", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInterface = ActivityMainBinding.inflate(layoutInflater)
        setContentView(userInterface.root)
        WebComp
        activity = this
        startProgressBar()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        handler.postDelayed({
            if (isGame) {
                val myIntent = Intent(this, GameActivity::class.java)
                startActivity(myIntent)
            }
        }, 5 * 1000)
    }

    private fun startProgressBar() {
        Thread {
            while (true) {
                handler.post { userInterface.progressBar.progress = pStatus }
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                pStatus++
                if (pStatus == 100) {
                    pStatus = 0
                }
            }
        }.start()
    }

    companion object WebComp {
        var isGame = true
        private var isfirst = true
        private var myEditor: Editor? = null
        private var myPreferences: SharedPreferences? = null

        @SuppressLint("StaticFieldLeak")
        var activity: MainActivity? = null

        @SuppressLint("CommitPrefEdits")
        fun communications(deep: String) {
            if (isfirst) {
                isfirst = false
                myPreferences = activity?.getSharedPreferences(AppConstants.CONFIG_NAME.data, MODE_PRIVATE)
                myEditor = myPreferences?.edit()
                BackgroundTask(
                    activity!!,
                    myPreferences!!,
                    activity!!,
                    deep
                ).run {
                    getGeo()
                    regEvent()
                }
            }
        }
    }
}