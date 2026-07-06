package io.gapalja.ddalkkakwatch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import io.gapalja.ddalkkakwatch.DdalkkakApp
import io.gapalja.ddalkkakwatch.MainActivity
import io.gapalja.ddalkkakwatch.ble.BleHidManager

/**
 * Bluetooth HID 연결을 백그라운드에서 유지하는 Foreground Service.
 *
 * 목적: Knock-Knock(손목 두 번) → QuickDictateActivity launch 시,
 * Bluetooth가 cold start면 연결에 1~2초 걸려 첫 트리거가 씹힘.
 * 이 서비스가 미리 연결을 warm하게 유지해 cold-start 지연을 줄인다.
 *
 * type=connectedDevice (Wear OS 14/15 FGS 요구).
 */
class DictationService : Service() {

    companion object {
        private const val TAG = "DDK_SVC"
        private const val CHANNEL_ID = "ddalkkak_keepalive"
        private const val NOTIF_ID = 7

        /** 어디서든 안전하게 시작 (이미 떠 있으면 onStartCommand만 재호출됨) */
        fun start(ctx: Context): Boolean {
            if (!hasRequiredBluetoothPermissions(ctx)) {
                Log.w(TAG, "start skipped: Bluetooth permissions not granted")
                return false
            }
            val i = Intent(ctx, DictationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
            return true
        }

        fun hasRequiredBluetoothPermissions(ctx: Context): Boolean =
            ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ctx.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }

    private lateinit var ble: BleHidManager

    override fun onCreate() {
        super.onCreate()
        ble = (application as DdalkkakApp).bleHidManager
        if (!hasRequiredBluetoothPermissions(this)) {
            Log.w(TAG, "onCreate stop: Bluetooth permissions not granted")
            stopSelf()
            return
        }
        createChannel()
        startInForeground()
        ble.register()
        Log.i(TAG, "onCreate — Bluetooth keep-alive 시작")
    }

    @android.annotation.SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasRequiredBluetoothPermissions(this)) {
            Log.w(TAG, "onStartCommand stop: Bluetooth permissions not granted")
            stopSelf()
            return START_NOT_STICKY
        }
        // 연결이 끊겨 있으면 다시 붙임 (Mac sleep→wake, 재부팅 등)
        if (!ble.isConnected()) {
            Log.i(TAG, "onStartCommand — 미연결 상태, connectToBondedMac 시도")
            ble.connectToBondedMac()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "딸깍 연결 유지",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "워치 ↔ Mac Bluetooth 연결을 백그라운드에서 유지합니다." }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun startInForeground() {
        val notif = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("딸깍 연결 유지 중")
            .setContentText("손목 두 번 두드림 = 받아쓰기")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
}
