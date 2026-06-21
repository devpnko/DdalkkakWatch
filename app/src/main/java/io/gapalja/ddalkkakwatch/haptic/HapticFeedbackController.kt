package io.gapalja.ddalkkakwatch.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedbackController {

    enum class Pattern { TAP, DOUBLE, LONG }

    fun trigger(ctx: Context, pattern: Pattern) {
        val v = vibrator(ctx) ?: return
        val effect = when (pattern) {
            Pattern.TAP -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            Pattern.DOUBLE -> VibrationEffect.createWaveform(longArrayOf(0, 50, 80, 50), -1)
            Pattern.LONG -> VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        v.vibrate(effect)
    }

    private fun vibrator(ctx: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
