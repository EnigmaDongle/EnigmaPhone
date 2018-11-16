package com.enigmadongle.enigmaphone;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public void startServerSocket(final MainActivity mainActivity) {

        Thread thread = new Thread(new Runnable() {
            private boolean end = false;
            private String stringData = null;
            private String TAG = "Server";

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(9002);

                    while (!end) {
                        //Server is waiting for client here, if needed
                        Socket socket = serverSocket.accept();
                        DataInputStream input = new DataInputStream(socket.getInputStream());

                        stringData = input.readUTF();
                        Log.e(TAG, "Received message " + stringData);

                        mainActivity.onSocketMessage(stringData);

                        if (stringData.equalsIgnoreCase("STOP")) {
                            end = true;
                            socket.close();
                            break;
                        }

                        socket.close();
                    }
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }
}

