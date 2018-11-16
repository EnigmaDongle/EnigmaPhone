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
import android.widget.Toast;

import com.enigmadongle.enigmaphone.dongle.CDCSerialDeviceSync;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;



public class MainActivity extends AppCompatActivity {

    private CDCSerialDeviceSync serialDevice = null;
    private TextView console = null;
    private byte[] readBuff = new byte[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        console = (TextView)findViewById(R.id.consoleText);
    }

    public void onButtonClick(View view){
        connectDevice(getApplicationContext());
    }

    public void connectDevice(Context context) {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            manager.requestPermission(device, mPermissionIntent);
            UsbDeviceConnection usbConnection = manager.openDevice(device);

            String conn = usbConnection.toString();

            Toast.makeText(context, conn, Toast.LENGTH_SHORT).show();

            serialDevice = new CDCSerialDeviceSync(device, usbConnection);

            serialDevice.syncOpen();
            serialDevice.debug(false);
            serialDevice.setBaudRate(115200);
            serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serialDevice.setParity(UsbSerialInterface.PARITY_NONE);
            serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

            final Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {

                    int status = serialDevice.syncRead(readBuff, 30);
                    if (status >= 0)
                        console.append(new String(readBuff));

                    handler.post(this);
                }
            };

            handler.post(r);

        }}

    public void sendToSerial(View view){
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                serialDevice.syncWrite("10".getBytes(), 30);
                console.append("10\n");
            }
        };
        handler.post(r);


    }
}