package com.enigmadongle.enigmaphone.dongle;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;

import com.felhr.usbserial.CDCSerialDevice;


public class CDCSerialDeviceSync extends CDCSerialDevice {
    public CDCSerialDeviceSync(UsbDevice device, UsbDeviceConnection connection) {
        super(device, connection);
    }

}
