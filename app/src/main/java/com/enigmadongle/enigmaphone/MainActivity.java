package com.enigmadongle.enigmaphone;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Iterator;



public class MainActivity extends AppCompatActivity {

    private UsbSerialDevice serialDevice = null;
    private TextView console = null;
    private long send = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        console = (TextView)findViewById(R.id.consoleText);
    }

    public void onButtonClick(View view){
        connectDevice(getApplicationContext());
    }

    public void connectDevice(final Context context) {
        final PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    manager.requestPermission(device, mPermissionIntent);
                    UsbDeviceConnection usbConnection = manager.openDevice(device);

                    serialDevice = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

                    serialDevice.open();
                    serialDevice.debug(false);
                    serialDevice.setBaudRate(19200);
                    serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                    serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                    serialDevice.read(new UsbSerialInterface.UsbReadCallback() {
                        @Override
                        public void onReceivedData(byte[] arg0)
                        {
                            long actual = System.currentTimeMillis();
                            console.append(new String(arg0) + ":  " + (actual - send));
                        }
                    });
                }
            }
        };
        handler.post(runnable);
    }

    public void sendToSerial(View view){
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                send = System.currentTimeMillis();
                serialDevice.write("10".getBytes());
                console.append("10\n");
            }
        };
        handler.post(r);
    }
}