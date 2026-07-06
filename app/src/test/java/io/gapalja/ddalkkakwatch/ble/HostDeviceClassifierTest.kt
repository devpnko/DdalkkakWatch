package io.gapalja.ddalkkakwatch.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostDeviceClassifierTest {
    @Test
    fun likelyMacNamesIncludeCommonAndCustomMacNames() {
        assertTrue(HostDeviceClassifier.isLikelyMacName("Hyuk MBP"))
        assertTrue(HostDeviceClassifier.isLikelyMacName("Mac Studio"))
        assertTrue(HostDeviceClassifier.isLikelyMacName("Office iMac"))
    }

    @Test
    fun likelyMacNamesExcludeGalaxyDevices() {
        assertFalse(HostDeviceClassifier.isLikelyMacName("Galaxy Watch"))
        assertFalse(HostDeviceClassifier.isLikelyMacName("Galaxy Mac Test"))
    }

    @Test
    fun hostKindPrefersMacNameThenComputerClass() {
        assertEquals("mac-like-host", HostDeviceClassifier.hostKind("Hyuk MBP", isComputerClass = false))
        assertEquals("computer-host", HostDeviceClassifier.hostKind("Office PC", isComputerClass = true))
        assertEquals("paired-device", HostDeviceClassifier.hostKind("Keyboard", isComputerClass = false))
        assertEquals("unknown-device", HostDeviceClassifier.hostKind("", isComputerClass = false))
    }

    @Test
    fun bluetoothAddressRedactionKeepsOnlySuffix() {
        val sampleAddress = listOf("AA", "BB", "CC", "DD", "EE", "FF").joinToString(":")
        assertEquals("**:**:**:**:EE:FF", HostDeviceClassifier.redactBluetoothAddress(sampleAddress))
        assertEquals("address-redacted", HostDeviceClassifier.redactBluetoothAddress(null))
        assertEquals("address-redacted", HostDeviceClassifier.redactBluetoothAddress("not-an-address"))
    }
}
