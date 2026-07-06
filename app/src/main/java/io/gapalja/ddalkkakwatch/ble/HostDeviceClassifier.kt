package io.gapalja.ddalkkakwatch.ble

internal object HostDeviceClassifier {
    fun isLikelyMacName(name: String): Boolean =
        name.contains("MacBook", ignoreCase = true) ||
            name.contains("Mac mini", ignoreCase = true) ||
            name.contains("Mac Studio", ignoreCase = true) ||
            name.contains("iMac", ignoreCase = true) ||
            name.contains("MBP", ignoreCase = true) ||
            (name.contains("Mac", ignoreCase = true) && !name.contains("Galaxy", ignoreCase = true))

    fun hostKind(name: String, isComputerClass: Boolean): String = when {
        isLikelyMacName(name) -> "mac-like-host"
        isComputerClass -> "computer-host"
        name.isNotBlank() -> "paired-device"
        else -> "unknown-device"
    }

    fun redactBluetoothAddress(address: String?): String {
        val parts = address?.split(":").orEmpty()
        return if (parts.size == 6) {
            "**:**:**:**:${parts[4]}:${parts[5]}"
        } else {
            "address-redacted"
        }
    }
}
