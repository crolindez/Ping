
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;



    public ClientSocketHandler(Handler handler, BluetoothDevice device ) {

        this.handler = handler;
        this.mDevice = device;

        Log.e(TAG,"ClientSocket created");
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(Constants.MY_UUID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        GameCommManager chat;

        // Make a connection to the BluetoothSocket
        try {
            mSocket.connect();
            Log.e(TAG,"ClientSocket connected");
            chat = new GameCommManager(mSocket, handler, true);
            new Thread(chat).start();
            Log.e(TAG,"ClientSocket GameComm start");
        } catch (IOException e) {
            // Close the socket
            Log.e(TAG,"ClientSocket connection failed");
            try {
                mSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

        }

    }

 /*   public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/



}
