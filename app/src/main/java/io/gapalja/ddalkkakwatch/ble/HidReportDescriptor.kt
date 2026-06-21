package io.gapalja.ddalkkakwatch.ble

/**
 * USB HID 1.11 Boot Protocol Keyboard descriptor (App E.6).
 * Mac/Windows/Linux 모두 driverless 인식.
 */
object HidReportDescriptor {

    val BOOT_KEYBOARD: ByteArray = byteArrayOf(
        0x05, 0x01,                                          // Usage Page (Generic Desktop)
        0x09, 0x06,                                          // Usage (Keyboard)
        0xA1.toByte(), 0x01,                                 // Collection (Application)
        // Modifier byte (byte 0): 8 bits, each = 1 modifier
        0x05, 0x07,                                          //   Usage Page (Key Codes)
        0x19, 0xE0.toByte(), 0x29, 0xE7.toByte(),            //   Usage Min/Max (LCtrl..RGUI)
        0x15, 0x00, 0x25, 0x01,                              //   Logical 0..1
        0x75, 0x01, 0x95.toByte(), 0x08,                     //   Report Size 1, Count 8
        0x81.toByte(), 0x02,                                 //   Input (Data, Var, Abs)
        // Reserved byte (byte 1)
        0x95.toByte(), 0x01, 0x75, 0x08, 0x81.toByte(), 0x01,
        // LED Output (5 bits + 3 const padding)
        0x95.toByte(), 0x05, 0x75, 0x01,
        0x05, 0x08, 0x19, 0x01, 0x29, 0x05,
        0x91.toByte(), 0x02,
        0x95.toByte(), 0x01, 0x75, 0x03, 0x91.toByte(), 0x01,
        // Keycode array (bytes 2..7), 6-key rollover
        0x95.toByte(), 0x06, 0x75, 0x08, 0x15, 0x00, 0x25, 0x65,
        0x05, 0x07, 0x19, 0x00, 0x29, 0x65,
        0x81.toByte(), 0x00,                                 //   Input (Data, Array)
        0xC0.toByte()                                        // End Collection
    )

    // ===== Modifier flags =====
    const val MOD_NONE = 0x00
    const val MOD_LCTRL = 0x01
    const val MOD_LSHIFT = 0x02
    const val MOD_LALT = 0x04
    const val MOD_LGUI = 0x08            // Cmd on Mac
    const val MOD_CMD_SHIFT = 0x0A       // LGUI | LSHIFT
    const val MOD_OPT_CMD = 0x0C         // LALT | LGUI — Typeless trigger

    // ===== Common HID Usage IDs (HID Usage Tables §10) =====
    const val KEY_A = 0x04
    const val KEY_X = 0x1B               // Typeless: Opt+Cmd+X
    const val KEY_ENTER = 0x28
    const val KEY_TAB = 0x2B
    const val KEY_ESC = 0x29
    const val KEY_SPACE = 0x2C
    const val KEY_BACKSLASH = 0x31
    const val KEY_PERIOD = 0x37
    const val KEY_SEMICOLON = 0x33
}
