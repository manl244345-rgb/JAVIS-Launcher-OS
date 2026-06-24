package com.javis.launcher.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.javis.launcher.models.OrbAnimationState
import kotlin.math.*

class OrbView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var state = OrbAnimationState.IDLE
    private var pulsePhase = 0f
    private var ringRotation = 0f
    private var glowAlpha = 180f
    private val animator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
        duration = 3000; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
        addUpdateListener { pulsePhase = it.animatedValue as Float; ringRotation += 1.2f; invalidate() }
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 4f }
    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val skullPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ledPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Colors
    private val COLOR_IDLE_RING = Color.parseColor("#CC0000")
    private val COLOR_LISTEN_RING = Color.parseColor("#00CCFF")
    private val COLOR_THINK_RING = Color.parseColor("#FFB300")
    private val COLOR_EXEC_RING = Color.parseColor("#00FF88")
    private val COLOR_SPEAK_RING = Color.parseColor("#FF4400")
    private val COLOR_BG = Color.parseColor("#0D0D1A")
    private val COLOR_GLOW = Color.parseColor("#220022")
    private val LED_GREEN = Color.parseColor("#00FF44")
    private val LED_RED = Color.parseColor("#FF2200")
    private val SKULL_LIGHT = Color.parseColor("#EEEEFF")
    private val SKULL_DARK = Color.parseColor("#888899")

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun setState(s: OrbAnimationState) {
        state = s
        if (!animator.isStarted) animator.start()
        invalidate()
    }

    private fun getRingColor() = when (state) {
        OrbAnimationState.IDLE -> COLOR_IDLE_RING
        OrbAnimationState.LISTENING -> COLOR_LISTEN_RING
        OrbAnimationState.THINKING -> COLOR_THINK_RING
        OrbAnimationState.EXECUTING -> COLOR_EXEC_RING
        OrbAnimationState.SPEAKING -> COLOR_SPEAK_RING
        OrbAnimationState.COMPLETED -> COLOR_EXEC_RING
        OrbAnimationState.ERROR -> COLOR_IDLE_RING
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f; val cy = height / 2f
        val r = (minOf(width, height) / 2f) * 0.92f

        // Outer glow
        val pulse = (sin(pulsePhase.toDouble()) * 0.3f + 0.7f).toFloat()
        glowPaint.shader = RadialGradient(cx, cy, r * 1.1f, intArrayOf(
            Color.argb((60 * pulse).toInt(), 200, 0, 0), Color.TRANSPARENT
        ), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r * 1.1f, glowPaint)

        // Background sphere
        bgPaint.shader = RadialGradient(cx - r * 0.2f, cy - r * 0.2f, r, intArrayOf(
            Color.parseColor("#1a1a2e"), Color.parseColor("#0D0D1A")
        ), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r, bgPaint)

        // Rotating energy ring
        val ringR = r * 0.88f
        ringPaint.color = getRingColor()
        ringPaint.alpha = (200 * pulse).toInt()
        ringPaint.strokeWidth = 5f
        val oval = RectF(cx - ringR, cy - ringR, cx + ringR, cy + ringR)
        canvas.save()
        canvas.rotate(ringRotation, cx, cy)
        canvas.drawArc(oval, 0f, 280f, false, ringPaint)
        canvas.restore()

        // Second ring (counter-rotating)
        ringPaint.strokeWidth = 2f
        ringPaint.alpha = (120 * pulse).toInt()
        val ring2R = r * 0.75f
        val oval2 = RectF(cx - ring2R, cy - ring2R, cx + ring2R, cy + ring2R)
        canvas.save()
        canvas.rotate(-ringRotation * 0.7f, cx, cy)
        canvas.drawArc(oval2, 20f, 200f, false, ringPaint)
        canvas.restore()

        // Skull face
        drawSkull(canvas, cx, cy, r * 0.55f)

        // Inner white glow behind skull
        innerGlowPaint.shader = RadialGradient(cx, cy, r * 0.4f, intArrayOf(
            Color.argb((80 * pulse).toInt(), 255, 255, 255), Color.TRANSPARENT
        ), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r * 0.4f, innerGlowPaint)

        // Status LEDs
        drawLEDs(canvas, cx, cy, r, pulse)
    }

    private fun drawSkull(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        skullPaint.color = SKULL_LIGHT
        // Cranium dome
        val craniumRect = RectF(cx - r * 0.8f, cy - r * 0.85f, cx + r * 0.8f, cy + r * 0.2f)
        canvas.drawOval(craniumRect, skullPaint)
        // Cheekbones
        skullPaint.color = SKULL_DARK
        val cheekRect = RectF(cx - r * 0.82f, cy - r * 0.2f, cx + r * 0.82f, cy + r * 0.4f)
        canvas.drawOval(cheekRect, skullPaint)
        // Jaw
        skullPaint.color = SKULL_LIGHT
        val jawRect = RectF(cx - r * 0.55f, cy + r * 0.1f, cx + r * 0.55f, cy + r * 0.7f)
        canvas.drawRoundRect(jawRect, r * 0.2f, r * 0.2f, skullPaint)
        // Eye sockets
        skullPaint.color = Color.parseColor("#0D0D1A")
        skullPaint.style = Paint.Style.FILL
        canvas.drawOval(RectF(cx - r * 0.55f, cy - r * 0.55f, cx - r * 0.05f, cy - r * 0.1f), skullPaint)
        canvas.drawOval(RectF(cx + r * 0.05f, cy - r * 0.55f, cx + r * 0.55f, cy - r * 0.1f), skullPaint)
        // Eye glow
        val eyeColor = getRingColor()
        skullPaint.color = Color.argb(200, Color.red(eyeColor), Color.green(eyeColor), Color.blue(eyeColor))
        val glowSize = r * 0.08f
        canvas.drawCircle(cx - r * 0.3f, cy - r * 0.32f, glowSize, skullPaint)
        canvas.drawCircle(cx + r * 0.3f, cy - r * 0.32f, glowSize, skullPaint)
        // Nose cavity
        skullPaint.color = Color.parseColor("#0D0D1A")
        canvas.drawPath(Path().apply {
            moveTo(cx, cy - r * 0.05f); lineTo(cx - r * 0.12f, cy + r * 0.15f)
            lineTo(cx + r * 0.12f, cy + r * 0.15f); close()
        }, skullPaint)
        // Teeth
        skullPaint.color = SKULL_LIGHT
        skullPaint.style = Paint.Style.FILL
        val toothW = r * 0.13f; val toothH = r * 0.2f
        val toothTop = cy + r * 0.35f
        for (i in -2..2) {
            val tx = cx + i * toothW * 1.05f
            canvas.drawRoundRect(RectF(tx - toothW * 0.4f, toothTop, tx + toothW * 0.4f, toothTop + toothH),
                toothW * 0.2f, toothW * 0.2f, skullPaint)
        }
        // Gaps between teeth
        skullPaint.color = Color.parseColor("#0D0D1A")
        for (i in -1..1) {
            val tx = cx + i * toothW * 1.05f + toothW * 0.52f
            canvas.drawRect(tx, toothTop, tx + toothW * 0.12f, toothTop + toothH * 0.8f, skullPaint)
        }
        skullPaint.style = Paint.Style.FILL
    }

    private fun drawLEDs(canvas: Canvas, cx: Float, cy: Float, r: Float, pulse: Float) {
        val ledPositions = listOf(-90f, -30f, 30f, 90f, 150f, 210f)
        ledPaint.maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        for ((i, angle) in ledPositions.withIndex()) {
            val rad = Math.toRadians(angle.toDouble())
            val lx = cx + cos(rad).toFloat() * r * 0.96f
            val ly = cy + sin(rad).toFloat() * r * 0.96f
            val isActive = when (state) {
                OrbAnimationState.IDLE -> i % 2 == 0
                OrbAnimationState.LISTENING -> true
                OrbAnimationState.THINKING -> (i + (ringRotation / 60).toInt()) % 2 == 0
                OrbAnimationState.EXECUTING -> true
                OrbAnimationState.SPEAKING -> sin(pulsePhase + i * 0.5f) > 0
                else -> i % 3 == 0
            }
            ledPaint.color = if (isActive) LED_GREEN else LED_RED
            ledPaint.alpha = if (isActive) (255 * pulse).toInt() else 80
            canvas.drawCircle(lx, ly, 4f, ledPaint)
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); animator.start() }
    override fun onDetachedFromWindow() { super.onDetachedFromWindow(); animator.cancel() }
}
