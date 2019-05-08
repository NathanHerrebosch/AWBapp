package com.example.awbapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class TcpClient {
    //the ip of the arduino
    private static final String SERVER_IP = "192.168.1.1";
    //the TCP port used
    private static final int SERVER_PORT = 8080;
    // message received from the server
    private String mServerMessage;
    // The message to be sent to the server
    private String messageOut = "";
    // sends message received notifications
    private OnMessageReceived mMessageListener;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    //the Tcp Socket
    private Socket socket;

    public TcpClient(OnMessageReceived mesListener) {
        mMessageListener = mesListener;
    }

    public void stopClient() {

        // send message that we are closing the connection
        SendMessage("Connection Closed by client");

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void SendMessage(String message) {
        messageOut = message;
        SendThread st = new SendThread();
        st.start();
    }

    public class SendThread extends Thread{

        @Override
        public void run(){
            if (mBufferOut != null && !mBufferOut.checkError()) {
                mBufferOut.println(messageOut);
                mBufferOut.flush();
            }
        }
    }

    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVER_PORT);
            Log.e("Tcp Client","Socket created");

            mRun = true;
            //sends the message to the server
            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            //receives the message which the server sends back
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            SendMessage("Client AWB connected");
            //in this while the client listens for the messages sent by the server
            while (mRun) {

                mServerMessage = mBufferIn.readLine();

                if (mServerMessage != null && mMessageListener != null) {
                    //call the method messageReceived from MyActivity class
                    mMessageListener.messageReceived("S: " + mServerMessage);
                    Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

                }
            }
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Declare the interface. The method messageReceived(String message) must be implemented in the MyActivity
    //class at asynckTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
