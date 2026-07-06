package io.gapalja.ddalkkakwatch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.gapalja.ddalkkakwatch.ble.HidReportDescriptor
import io.gapalja.ddalkkakwatch.haptic.HapticFeedbackController
import io.gapalja.ddalkkakwatch.service.DictationService

/**
 * "딸깍 받아쓰기" — Knock-Knock(손목 두 번)에 할당하는 무화면 진입점.
 *
 * Galaxy Watch 설정 → 고급 기능 → 손동작(Knock-Knock) → "딸깍 받아쓰기" 선택.
 * 손목 두드림 → 이 Activity가 launch → 화면 안 띄우고 Opt+Cmd+X 한 발 → finish.
 *
 * OpenTypeless / Superwhisper가 "tap-to-toggle"이면 한 발로 받아쓰기 ON,
 * 다시 두드리면 OFF. (hold 방식은 워치 화면 PTT로 — MainActivity)
 *
 * 투명 테마라 사용자에겐 "워치 화면 0번, 손목만 두 번" 경험.
 */
class QuickDictateActivity : Activity() {

    companion object { private const val TAG = "DDK_QUICK" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!DictationService.hasRequiredBluetoothPermissions(this)) {
            Log.w(TAG, "Bluetooth 권한 없음 → MainActivity로 권한 요청 유도")
            startActivity(Intent(this, MainActivity::class.java))
            finishWithoutAnimation()
            return
        }

        // 서비스가 안 떠 있을 수도 있으니 보장 (Bluetooth keep-alive)
        DictationService.start(this)

        val ble = (application as DdalkkakApp).bleHidManager

        if (ble.isConnected()) {
            Log.i(TAG, "연결됨 → Opt+Cmd+X 토글 전송")
            ble.sendKey(HidReportDescriptor.MOD_OPT_CMD, HidReportDescriptor.KEY_X)
            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.DOUBLE)
        } else {
            // 첫 호출이라 cold면: 연결만 시작, 사용자에게 "한 번 더" 신호 (롱 햅틱)
            Log.i(TAG, "미연결 → connectToBondedMac (이번엔 연결만, 다시 두드리면 작동)")
            ble.connectToBondedMac()
            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.LONG)
        }

        finishWithoutAnimation()
    }

    @Suppress("DEPRECATION")
    private fun finishWithoutAnimation() {
        finish()
        overridePendingTransition(0, 0)
    }
}
