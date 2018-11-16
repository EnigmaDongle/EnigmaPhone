package com.enigmadongle.enigmaphone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextViewReplyFromServer;
    private EditText mEditTextSendMessage;
    private String ipKmiotek = "10.147.17.184";
    private String ipJa = "10.147.17.190";
    private String ip = null;
    private boolean end = false;
    private String TAG = "MainActivity";
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonSend = findViewById(R.id.sendBtn);

        Button ip1 = findViewById(R.id.ip1);
        Button ip2 = findViewById(R.id.ip2);

        mEditTextSendMessage = findViewById(R.id.editText);
        mTextViewReplyFromServer = findViewById(R.id.textView);

        buttonSend.setOnClickListener(this);
        startServerSocket();
        ip1.setText("Kmiotek");
        ip2.setText("Ja");
        ip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = ipKmiotek;
            }
        });
        ip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = ipJa;
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendBtn:
                sendMessage(mEditTextSendMessage.getText().toString());
                break;
        }
    }

    private void startServerSocket() {

        Thread thread = new Thread(new Runnable() {

            private String stringData = null;

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

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mTextViewReplyFromServer.setText(stringData);
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

    private void sendMessage(final String msg) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(socket == null){
                    try {
                        socket = new Socket(ip, 9002);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (!socket.getInetAddress().toString().equals(ip)){
                    try {
                        socket = new Socket(ip, 9002);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG, "Sending message " + msg + " to ip " + ip);
                try {
//                    socket = new Socket(ip, 9002);
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
