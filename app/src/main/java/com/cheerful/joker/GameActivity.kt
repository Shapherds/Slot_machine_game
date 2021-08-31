package com.cheerful.joker

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cheerful.joker.databinding.ActivityGameBinding
import java.util.*

class GameActivity : AppCompatActivity(), IsEnd {

    lateinit var binding: ActivityGameBinding
    var handler = Handler(Looper.getMainLooper())
    var count_down = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity =this
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun spin(view: View) {
        if (isEnd) {
            isEnd = false
            setSpinValue()
            handler.postDelayed({ isWin() }, 3000)
        }
    }

    private fun setSpinValue() {
        binding.im1.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im2.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im3.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im4.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im5.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im6.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im7.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im8.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
        binding.im9.setValueRandom(Random().nextInt(6), Random().nextInt(15 - 6 + 1) + 5, this)
    }

    private fun isWin() {
        if (binding.im1.getImage() == binding.im2.getImage()
            && binding.im1.getImage() == binding.im3.getImage()
        ) {
            showDialog(true)
        } else if (binding.im4.getImage() == binding.im5.getImage()
            && binding.im4.getImage() == binding.im6.getImage()
        ) {
            showDialog(true)
        } else showDialog(
                binding.im7.getImage() == binding.im8.getImage()
                    && binding.im7.getImage() == binding.im9.getImage()
        )
    }

    private fun showDialog(isWin: Boolean) {
        val myDialogFragment = GameMessage(isWin)
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        myDialogFragment.show(transaction, "dialog")
    }

    fun info(view: View) {}
    override fun whenEnd(result: Int, count: Int) {
        if (count_down < 2) {
            count_down++
        } else {
            count_down = 0
        }
    }
    companion object gameData {
        var activity: GameActivity? = null
        var isEnd = true
    }
}