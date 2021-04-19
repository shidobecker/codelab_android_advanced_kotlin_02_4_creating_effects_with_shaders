package com.example.android.findme

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.floor
import kotlin.random.Random

class SpotlightImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var paint = Paint()
    private var shouldDrawSpotLight = false
    private var gameOver = false

    private lateinit var winnerRect: RectF
    private var androidBitmapX = 0f
    private var androidBitmapY = 0f

    private val bitmapAndroid = BitmapFactory.decodeResource(
        resources,
        R.drawable.android
    )
    private val spotlight = BitmapFactory.decodeResource(resources, R.drawable.mask)

    private var shader: Shader


    /**
     * When the user touches and holds the screen for the spotlight, instead of calculating where
     * the spotlight needs to be drawn, you move the shader matrix; that is, the texture/shader
     * coordinate system, and then draw the texture (the spotlight) at the same location in the
     * translated coordinate system. The resulting effect will seem as if you are drawing the
     * spotlight texture at a different location, which is the same as the shader matrix translated
     * location. This is simpler and slightly more efficient.
     */
    private val shaderMatrix = Matrix()


    init {
        val bitmap = Bitmap.createBitmap(spotlight.width, spotlight.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        shaderPaint.color = Color.BLACK
        canvas.drawRect(
            0.0F,
            0.0F,
            spotlight.width.toFloat(),
            spotlight.height.toFloat(),
            shaderPaint
        )

        shaderPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        canvas.drawBitmap(spotlight, 0.0F, 0.0f, shaderPaint)

        shader = BitmapShader(
            bitmap,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        ) //https://developer.android.com/codelabs/advanced-android-kotlin-training-shaders#4

        paint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)
        //canvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)

       // shaderMatrix.setTranslate(100f, 550f)

       // shader.setLocalMatrix(shaderMatrix)

        //canvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat()/2, paint)
        canvas.drawBitmap(bitmapAndroid, androidBitmapX, androidBitmapY, paint)

        if (!gameOver) {
            if (shouldDrawSpotLight) {
                canvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
            } else {
                canvas.drawColor(Color.BLACK)
            }
        }

    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val motionEventX = motionEvent.x
        val motionEventY = motionEvent.y

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                shouldDrawSpotLight = true
                if (gameOver) {
                    gameOver = false
                    setupWinnerRect()
                }
            }
            MotionEvent.ACTION_UP -> {
                shouldDrawSpotLight = false
                gameOver = winnerRect.contains(motionEventX, motionEventY)
            }
        }
        shaderMatrix.setTranslate(
            motionEventX - spotlight.width / 2.0f,
            motionEventY - spotlight.height / 2.0f
        )
        shader.setLocalMatrix(shaderMatrix)
        invalidate()
        return true
    }

    private fun setupWinnerRect() {
        androidBitmapX = floor(Random.nextFloat() * (width - bitmapAndroid.width))
        androidBitmapY = floor(Random.nextFloat() * (height - bitmapAndroid.height))

        winnerRect = RectF(
            (androidBitmapX),
            (androidBitmapY),
            (androidBitmapX + bitmapAndroid.width),
            (androidBitmapY + bitmapAndroid.height)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setupWinnerRect()
    }

    /**
     * The tiling mode, TileMode, defined in the Shader, specifies how the bitmap drawable is repeated
     * or mirrored in the X and Y directions if the bitmap drawable being used for texture is smaller
     * than the screen. Android provides three different ways to repeat (tile) the bitmap drawable (texture):

    REPEAT : Repeats the bitmap shader's image horizontally and vertically.
    CLAMP : The edge colors will be used to fill the extra space outside of the shader's image bounds.
    MIRROR : The shader's image is mirrored horizontally and vertically.
     */


    /**
     * If the size of the object being drawn (like the rectangle in the above step) is larger than the
     * texture, which is usually the case. You can tile the bitmap texture in different ways - CLAMP,
     * REPEAT, and MIRROR. The tiling mode for the shader you created in the previous task is CLAMP,
     * since you only want to draw the spotlight once and fill in the rest with black.

     */


}