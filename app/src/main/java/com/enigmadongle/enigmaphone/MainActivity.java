package com.enigmadongle.enigmaphone;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    Button startButton, connectButton, stopButton;
    TextView textView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    EditText ipText;

    Connector connector;
    Server server;

    Boolean isSerialReady = false;
    Boolean isConnectionReady = false;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data =  new String(arg0);
            if (isConnectionReady)
                serialPort.write(data.getBytes());
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.syncOpen()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
//                            serialPort.setBaudRate(921600);
//                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
//                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
//                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
//                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                            tvAppend(textView, "Serial Connection Opened!\n");
                            isSerialReady = true;

                            Thread echo = new Thread(new Runnable() {
                                final int bufferSize = 256;
                                @Override
                                public void run() {
                                    byte[] rollingBuffer = new byte[bufferSize];
                                    int bytesRead = 0;

                                    while (true) {
                                        byte[] readBuffer = new byte[bufferSize];
                                        int readSize = serialPort.syncRead(readBuffer, 5);
                                        if (readSize > 0) {
                                            bytesRead += readSize;
                                            Log.e("APP", "Read " + bytesRead + " bytes.");

                                            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize*2);
                                            byteBuffer.put(Arrays.copyOfRange(rollingBuffer, 0, bytesRead - readSize));
                                            byteBuffer.put(Arrays.copyOfRange(readBuffer, 0, readSize));

                                            if(bytesRead >= bufferSize) {
                                                byte[] chunk = Arrays.copyOfRange(byteBuffer.array(), 0, bufferSize);
                                                serialPort.syncWrite(chunk, 5);
                                                rollingBuffer = Arrays.copyOfRange(byteBuffer.array(), bufferSize, bytesRead);
                                                bytesRead -= bufferSize;
                                            } else {
                                                rollingBuffer = Arrays.copyOfRange(byteBuffer.array(), 0, bytesRead);
                                            }
                                        }
                                    }
                                }
                            });

                            echo.start();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }


    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart);
        connectButton = (Button) findViewById(R.id.buttonConnect);
        stopButton = (Button) findViewById(R.id.buttonStop);
        ipText = (EditText) findViewById(R.id.ipAddr);
        textView = (TextView) findViewById(R.id.textView);

        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        server = new Server();
        server.startServerSocket(this);
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        stopButton.setEnabled(bool);
        connectButton.setEnabled(bool);
        textView.setEnabled(bool);
    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, pi);
            }
        }
    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView, "\nSerial Connection Closed! \n");

    }

    public void onClickConnectServer(View view) {
        connector = new Connector(ipText.getText().toString());
        isConnectionReady = true;
    }

    public void onSocketMessage(String msg){
        tvAppend(textView, "Received from network: " + msg.length());
        if (isSerialReady)
            serialPort.write(msg.getBytes());
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ftv.getLineCount() > 25)
                    ftv.setText("");
                ftv.append(ftext);
            }
        });
    }

}
