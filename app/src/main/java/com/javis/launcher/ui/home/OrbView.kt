package com.javis.launcher.ui.home

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.javis.launcher.models.OrbAnimationState
import kotlin.math.min
import kotlin.math.sin

class OrbView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var currentState = OrbAnimationState.IDLE
    private var pulseAnimator: ValueAnimator? = null
    private var rotateAnimator: ValueAnimator? = null
    private var pulseRadius = 0f
    private var ringRotation = 0f
    private var glowAlpha = 180
    private var time = 0f

    private val skullPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A1A2E") }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2222"); style = Paint.Style.STROKE; strokeWidth = 6f
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00FF88"); style = Paint.Style.FILL
    }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF2222") }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0D0D1A") }

    init { startIdleAnimation() }

    fun setState(state: OrbAnimationState) {
        if (currentState == state) return
        currentState = state
        pulseAnimator?.cancel()
        rotateAnimator?.cancel()
        when (state) {
            OrbAnimationState.IDLE -> startIdleAnimation()
            OrbAnimationState.LISTENING -> startListeningAnimation()
            OrbAnimationState.THINKING -> startThinkingAnimation()
            OrbAnimationState.EXECUTING -> startExecutingAnimation()
            OrbAnimationState.SPEAKING -> startSpeakingAnimation()
            OrbAnimationState.COMPLETED -> startCompletedAnimation()
            OrbAnimationState.ERROR -> startErrorAnimation()
        }
    }

    private fun startIdleAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000; repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
            addUpdateListener { time = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun startListeningAnimation() {
        ringPaint.color = Color.parseColor("#00AAFF")
        rotateAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1000; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { ringRotation = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun startThinkingAnimation() {
        ringPaint.color = Color.parseColor("#FFAA00")
        rotateAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 800; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { ringRotation = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun startExecutingAnimation() {
        ringPaint.color = Color.parseColor("#FF6600")
        rotateAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 500; repeatCount = ValueAnimator.INFINITE
            addUpdateListener { ringRotation = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun startSpeakingAnimation() {
        ringPaint.color = Color.parseColor("#AA00FF")
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400; repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
            addUpdateListener { time = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    private fun startCompletedAnimation() {
        ringPaint.color = Color.parseColor("#00FF88")
        invalidate()
    }

    private fun startErrorAnimation() {
        ringPaint.color = Color.parseColor("#FF0000")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f * 0.85f

        // Background circle
        canvas.drawCircle(cx, cy, r, bgPaint)

        // Outer glow
        val glowR = r + 8f + sin(time * Math.PI).toFloat() * 4f
        val glowShader = RadialGradient(cx, cy, glowR, intArrayOf(ringPaint.color and 0x40FFFFFF.toInt(), Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        glowPaint.shader = glowShader
        canvas.drawCircle(cx, cy, glowR, glowPaint)
        glowPaint.shader = null

        // Rotating energy ring
        canvas.save()
        canvas.rotate(ringRotation, cx, cy)
        for (i in 0..7) {
            val angle = i * 45f
            val segSweep = 30f
            canvas.drawArc(cx - r, cy - r, cx + r, cy + r, angle, segSweep, false, ringPaint)
        }
        canvas.restore()

        // Main skull orb body
        canvas.drawCircle(cx, cy, r * 0.8f, skullPaint)

        // Skull shape (simplified)
        val skullW = r * 0.5f
        val skullH = r * 0.6f
        val skullTop = cy - skullH * 0.6f
        val skullPath = Path().apply {
            addRoundRect(cx - skullW / 2, skullTop, cx + skullW / 2, cy + skullH * 0.2f, skullW * 0.4f, skullW * 0.4f, Path.Direction.CW)
        }
        canvas.drawPath(skullPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2D1B69") })

        // Glowing eyes
        val eyeY = cy - skullH * 0.05f
        val eyeRadius = r * 0.12f
        val eyeGlowSize = eyeRadius + sin(time * Math.PI * 2).toFloat() * 3f
        val eyeColor = ringPaint.color
        val eyeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx - r * 0.18f, eyeY, eyeGlowSize, intArrayOf(eyeColor, Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx - r * 0.18f, eyeY, eyeGlowSize, eyeGlowPaint)
        canvas.drawCircle(cx + r * 0.18f, eyeY, eyeGlowSize, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx + r * 0.18f, eyeY, eyeGlowSize, intArrayOf(eyeColor, Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
        })
        canvas.drawCircle(cx - r * 0.18f, eyeY, eyeRadius * 0.6f, eyePaint.apply { color = eyeColor })
        canvas.drawCircle(cx + r * 0.18f, eyeY, eyeRadius * 0.6f, eyePaint)

        // Status dots (green)
        val dotY = cy + r * 0.5f
        for (i in -1..1) {
            statusPaint.alpha = if (i == 0) 255 else 180
            canvas.drawCircle(cx + i * r * 0.2f, dotY, r * 0.04f, statusPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator?.cancel()
        rotateAnimator?.cancel()
    }
}
