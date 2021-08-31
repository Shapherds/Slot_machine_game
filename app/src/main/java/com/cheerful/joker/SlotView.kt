package com.cheerful.joker

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

class SlotView: FrameLayout {
    private val ANUMATUIN_DUR = 150
    private lateinit var curentImage: ImageView
    private lateinit var nextImage: ImageView
    private var lastResult = 0
    private var old_value: Int = 0
    private var imageId = 0
    lateinit var ScroollingEnd: IsEnd
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.custom_view_drum, this)
        curentImage = rootView.findViewById(R.id.curent)
        nextImage = rootView.findViewById(R.id.next_image)
        nextImage.translationY = height.toFloat()
    }

    fun setValueRandom(image: Int, num: Int , isScroollingEnd: IsEnd) {
        ScroollingEnd = isScroollingEnd
        curentImage.animate()
            .translationY(-height.toFloat())
            .setDuration(ANUMATUIN_DUR.toLong())
            .setInterpolator(AccelerateInterpolator())
            .start()
        nextImage.translationY = curentImage.height.toFloat()
        nextImage.animate()
            .translationY(0f)
            .setDuration(ANUMATUIN_DUR.toLong())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        setImage(curentImage, old_value % 5)
                        curentImage.translationY = 0f
                        if (old_value != num) {
                            setValueRandom(image, num ,ScroollingEnd)
                            old_value++
                        } else {
                            lastResult = 0
                            old_value = 0
                            setImage(nextImage, image)
                            isScroollingEnd.whenEnd(image % 6, num)
                        }
                    }, 80)
                }

                override fun onAnimationEnd(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
    }

    private fun setImage(curentImage: ImageView, i: Int) {
        imageId = when (i) {
            0 -> {
                curentImage.setImageResource(R.drawable.im1)
                R.drawable.im1
            }
            1 -> {
                curentImage.setImageResource(R.drawable.im2)
                R.drawable.im2
            }
            2 -> {
                curentImage.setImageResource(R.drawable.im3)
                R.drawable.im3
            }
            3 -> {
                curentImage.setImageResource(R.drawable.im4)
                R.drawable.im4
            }
            4 -> {
                curentImage.setImageResource(R.drawable.im5)
                R.drawable.im5
            }
            else -> {
                curentImage.setImageResource(R.drawable.im6)
                R.drawable.im6
            }
        }
        lastResult = i
    }

    fun getImage(): Int {
        return imageId
    }
}