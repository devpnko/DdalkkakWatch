package io.gapalja.ddalkkakwatch.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

class BleHidManager(private val ctx: Context) {

    companion object { private const val TAG = "DDK_BLE" }

    private val adapter: BluetoothAdapter? by lazy {
        (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedHost: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState

    private val _appRegistered = MutableStateFlow(false)
    val appRegistered: StateFlow<Boolean> = _appRegistered

    private val executor = Executors.newSingleThreadExecutor()

    private val sdpSettings: BluetoothHidDeviceAppSdpSettings by lazy {
        BluetoothHidDeviceAppSdpSettings(
            "DdalkkakWatch",
            "Dictation trigger (HID keyboard)",
            "DdalkkakWatch",
            BluetoothHidDevice.SUBCLASS1_KEYBOARD,
            HidReportDescriptor.BOOT_KEYBOARD
        )
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(device: BluetoothDevice?, registered: Boolean) {
            Log.i(TAG, "onAppStatusChanged registered=$registered")
            _appRegistered.value = registered
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            val stateStr = when (state) {
                BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
                BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
                BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
                else -> "OTHER($state)"
            }
            Log.i(TAG, "onConnectionStateChanged device=${device.address} state=$stateStr")
            connectedHost = if (state == BluetoothProfile.STATE_CONNECTED) device else null
            _connectionState.value = state
        }

        override fun onInterruptData(device: BluetoothDevice, reportId: Byte, data: ByteArray) {
            // Mac → 워치 LED state 등 (무시)
        }
    }

    @SuppressLint("MissingPermission")
    fun register() {
        if (_appRegistered.value && hidDevice != null) {
            Log.i(TAG, "register: 이미 등록됨, skip (singleton)")
            return
        }
        val a = adapter ?: run {
            Log.e(TAG, "BluetoothAdapter null — BT not supported")
            return
        }
        if (!a.isEnabled) {
            Log.e(TAG, "Bluetooth disabled — 사용자가 BT 켜야 함")
            return
        }

        Log.i(TAG, "register() — getProfileProxy(HID_DEVICE) 호출")
        val ok = a.getProfileProxy(ctx, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                Log.i(TAG, "onServiceConnected profile=$profile")
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    val regOk = hidDevice?.registerApp(sdpSettings, null, null, executor, hidCallback) ?: false
                    Log.i(TAG, "registerApp 결과: $regOk")
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                Log.w(TAG, "onServiceDisconnected profile=$profile")
                hidDevice = null
                _appRegistered.value = false
            }
        }, BluetoothProfile.HID_DEVICE)
        Log.i(TAG, "getProfileProxy 호출 결과: $ok")
    }

    @SuppressLint("MissingPermission")
    fun connectToBondedMac(): Boolean {
        val a = adapter ?: run { Log.e(TAG, "adapter null"); return false }
        val hid = hidDevice ?: run { Log.e(TAG, "hidDevice null"); return false }
        val bonded = a.bondedDevices
        Log.i(TAG, "페어링된 디바이스: ${bonded?.map { "${it.name}/${it.address}" }}")
        val mac = bonded?.firstOrNull { dev ->
            val name = dev.name ?: ""
            name.contains("MacBook", ignoreCase = true) ||
                    name.contains("iMac", ignoreCase = true) ||
                    (name.contains("Mac", ignoreCase = true) && !name.contains("Galaxy", ignoreCase = true))
        }
        if (mac == null) {
            Log.w(TAG, "페어링된 Mac 없음. Mac BT에서 'DdalkkakWatch' 또는 워치 이름으로 페어링 후 재시도")
            return false
        }
        Log.i(TAG, "Mac 발견: ${mac.name} (${mac.address}). hidDevice.connect() 시도")
        val ok = hid.connect(mac)
        Log.i(TAG, "hidDevice.connect(${mac.name}) = $ok")
        return ok
    }

    @SuppressLint("MissingPermission")
    fun sendKey(modifier: Int, keycode: Int) {
        val device = connectedHost
        if (device == null) {
            Log.w(TAG, "sendKey: connectedHost null — connectToBondedMac 자동 호출")
            connectToBondedMac()
            return
        }
        // 실시간 HID profile state 확인 (singleton stale 회피)
        val realState = try { hidDevice?.getConnectionState(device) } catch (e: Exception) { null }
        if (realState != BluetoothProfile.STATE_CONNECTED) {
            Log.w(TAG, "sendKey: stale state — real=$realState. connectedHost reset + reconnect")
            connectedHost = null
            _connectionState.value = BluetoothProfile.STATE_DISCONNECTED
            // 다시 connect 시도 (다음 tap에 send 작동)
            hidDevice?.connect(device)
            return
        }
        val press = byteArrayOf(
            modifier.toByte(), 0x00,
            keycode.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00
        )
        val release = ByteArray(8)
        try {
            val pOk = hidDevice?.sendReport(device, 0, press) ?: false
            Thread.sleep(40)
            val rOk = hidDevice?.sendReport(device, 0, release) ?: false
            Log.i(TAG, "sendKey mod=$modifier key=$keycode press=$pOk release=$rOk → ${device.address}")
        } catch (e: Exception) {
            Log.e(TAG, "sendKey 실패: ${e.message}")
        }
    }

    /** PTT용: key press만 송신 (release 안 함, 사용자가 누르고 있는 동안 hold) */
    @SuppressLint("MissingPermission")
    fun holdKey(modifier: Int, keycode: Int) {
        val device = connectedHost ?: run {
            Log.w(TAG, "holdKey: connectedHost null — reconnect")
            connectToBondedMac()
            return
        }
        val press = byteArrayOf(
            modifier.toByte(), 0x00,
            keycode.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00
        )
        try {
            val ok = hidDevice?.sendReport(device, 0, press) ?: false
            Log.i(TAG, "holdKey mod=$modifier key=$keycode press=$ok → ${device.address}")
        } catch (e: Exception) {
            Log.e(TAG, "holdKey 실패: ${e.message}")
        }
    }

    /** PTT용: 모든 키 release (8-byte 0) */
    @SuppressLint("MissingPermission")
    fun releaseAll() {
        val device = connectedHost ?: return
        try {
            val ok = hidDevice?.sendReport(device, 0, ByteArray(8)) ?: false
            Log.i(TAG, "releaseAll release=$ok")
        } catch (e: Exception) {
            Log.e(TAG, "releaseAll 실패: ${e.message}")
        }
    }

    /** 현재 Mac에 연결되어 있는지 (UI/QuickDictate 공용) */
    fun isConnected(): Boolean =
        _connectionState.value == BluetoothProfile.STATE_CONNECTED

    @SuppressLint("MissingPermission")
    fun release() {
        try { hidDevice?.unregisterApp() } catch (_: Exception) {}
        hidDevice = null
    }
}
