package io.gapalja.ddalkkakwatch

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import io.gapalja.ddalkkakwatch.ble.BleHidManager
import io.gapalja.ddalkkakwatch.ble.HidReportDescriptor
import io.gapalja.ddalkkakwatch.haptic.HapticFeedbackController
import io.gapalja.ddalkkakwatch.service.DictationService
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {

    companion object { private const val TAG = "DDK" }

    private lateinit var bleHidManager: BleHidManager
    private var discoverableRequested = false

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            Log.i(TAG, "권한 결과: $result allGranted=$allGranted")
            if (allGranted) registerAndDiscoverable()
        }

    private val discoverableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i(TAG, "ACTION_REQUEST_DISCOVERABLE 결과: ${result.resultCode}")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.i(TAG, "onCreate — PTT + DoubleTap")

        bleHidManager = (application as DdalkkakApp).bleHidManager
        ensurePermissionsThenRegister()

        setContent {
            MaterialTheme {
                val connState by bleHidManager.connectionState.collectAsState()
                val registered by bleHidManager.appRegistered.collectAsState()
                val connected = connState == BluetoothProfile.STATE_CONNECTED

                fun isConnected() = bleHidManager.connectionState.value == BluetoothProfile.STATE_CONNECTED

                GestureScreen(
                    statusText = when {
                        !registered -> "HID 등록 중..."
                        connected -> "누름=Typeless · 더블탭=Enter"
                        else -> "탭=연결"
                    },
                    onPttStart = {
                        Log.i(TAG, "PTT 시작 connected=${isConnected()}")
                        if (!isConnected()) {
                            bleHidManager.connectToBondedMac()
                            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.TAP)
                        } else {
                            bleHidManager.holdKey(
                                HidReportDescriptor.MOD_OPT_CMD,
                                HidReportDescriptor.KEY_X
                            )
                            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.TAP)
                        }
                    },
                    onPttEnd = {
                        Log.i(TAG, "PTT 종료")
                        if (isConnected()) {
                            bleHidManager.releaseAll()
                            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.DOUBLE)
                        }
                    },
                    onDoubleTap = {
                        Log.i(TAG, "double tap = Enter")
                        if (isConnected()) {
                            bleHidManager.sendKey(
                                HidReportDescriptor.MOD_NONE,
                                HidReportDescriptor.KEY_ENTER
                            )
                            HapticFeedbackController.trigger(this, HapticFeedbackController.Pattern.DOUBLE)
                        }
                    }
                )
            }
        }
    }

    override fun onPause() {
        if (::bleHidManager.isInitialized) {
            bleHidManager.releaseAll()
        }
        super.onPause()
    }

    private fun ensurePermissionsThenRegister() {
        val needed = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) registerAndDiscoverable()
        else requestPermissions.launch(missing.toTypedArray())
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun registerAndDiscoverable() {
        bleHidManager.register()
        // Bluetooth 연결을 백그라운드에서 유지해 Knock-Knock cold-start 지연을 줄인다.
        // Android 12+에서는 Bluetooth 권한 승인 전에 FGS가 HID 등록을 시도하면 실패할 수 있다.
        DictationService.start(this)
        try {
            val adapter = (getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
            adapter?.setName("DdalkkakWatch")
        } catch (_: Exception) {}
        if (!discoverableRequested) {
            discoverableRequested = true
            try {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                }
                discoverableLauncher.launch(intent)
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun GestureScreen(
    statusText: String,
    onPttStart: () -> Unit,
    onPttEnd: () -> Unit,
    onDoubleTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    // 150ms 안에 떼면 = quick tap (double-tap 후보)
                    var quickUp = false
                    withTimeoutOrNull(150L) {
                        waitForUpOrCancellation()
                        quickUp = true
                    }

                    if (quickUp) {
                        // 짧게 떼짐 → 250ms 안에 두 번째 down 대기 = double tap
                        val secondDown = withTimeoutOrNull(250L) {
                            awaitFirstDown(requireUnconsumed = false)
                        }
                        if (secondDown != null) {
                            onDoubleTap()
                            // 두 번째 손가락 떼기 대기 (release 정리)
                            waitForUpOrCancellation()
                        }
                        // 단일 짧 탭이면 아무것도 안 함 (false trigger 방지)
                    } else {
                        // 150ms 이상 누름 → PTT 시작
                        onPttStart()
                        waitForUpOrCancellation()
                        onPttEnd()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "딸깍",
                color = Color.White,
                style = MaterialTheme.typography.display1
            )
            Text(
                text = statusText,
                color = Color(0xFFAAAAAA),
                style = MaterialTheme.typography.caption1
            )
        }
    }
}
