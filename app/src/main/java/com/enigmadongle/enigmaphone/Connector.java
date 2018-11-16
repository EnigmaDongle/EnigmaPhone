package com.enigmadongle.enigmaphone;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Connector {
    private String ipKmiotek = "192.168.43.95";
    private String ipJa = "192.168.43.162";
    private String ip;
    private boolean end = false;
    private String TAG = "DupaActivity";
    private Socket socket;

    public Connector(String serverIp) {
        ip = serverIp;
    }

    public void sendMessage(final String msg) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Trying to send message " + msg + " to ip " + ip);
                if (socket == null) {
                    try {
                        socket = new Socket(ip, 9002);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (!socket.getInetAddress().toString().equals(ip)) {
                    try {
                        socket = new Socket(ip, 9002);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG, "Sending message " + msg + " to ip " + ip);
                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    out.writeUTF(msg);

                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}