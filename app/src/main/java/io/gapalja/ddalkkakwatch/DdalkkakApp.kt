package io.gapalja.ddalkkakwatch

import android.app.Application
import io.gapalja.ddalkkakwatch.ble.BleHidManager

class DdalkkakApp : Application() {
    val bleHidManager: BleHidManager by lazy { BleHidManager(applicationContext) }
}
